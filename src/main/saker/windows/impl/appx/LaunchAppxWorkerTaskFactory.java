package saker.windows.impl.appx;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.NavigableMap;

import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.SakerLog;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.task.identifier.TaskIdentifier;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.build.trace.BuildTrace;
import saker.build.util.property.BuildTimeExecutionProperty;
import saker.process.api.CollectingProcessIOConsumer;
import saker.process.api.SakerProcess;
import saker.process.api.SakerProcessBuilder;
import saker.sdk.support.api.SDKDescription;
import saker.sdk.support.api.SDKReference;
import saker.sdk.support.api.SDKSupportUtils;
import saker.sdk.support.api.exc.SDKPathNotFoundException;
import saker.windows.api.SakerWindowsUtils;
import saker.windows.main.appx.LaunchAppxTaskFactory;

public abstract class LaunchAppxWorkerTaskFactory
		implements Task<Object>, TaskFactory<Object>, TaskIdentifier, Externalizable {
	private static final long serialVersionUID = 1L;

	private NavigableMap<String, SDKDescription> sdks;

	/**
	 * For {@link Externalizable}.
	 */
	public LaunchAppxWorkerTaskFactory() {
	}

	public void setSdks(NavigableMap<String, SDKDescription> sdks) {
		this.sdks = ImmutableUtils.makeImmutableNavigableMap(sdks);
	}

	@Override
	public Object run(TaskContext taskcontext) throws Exception {
		if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
			BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_META);
		}
		taskcontext.setStandardOutDisplayIdentifier(LaunchAppxTaskFactory.TASK_NAME);

		NavigableMap<String, SDKReference> sdkrefs = SDKSupportUtils.resolveSDKReferences(taskcontext, sdks);
		SDKReference appcertkitsdk = SDKSupportUtils.requireSDK(sdkrefs, SakerWindowsUtils.SDK_NAME_WINDOWSAPPCERTKIT);
		SakerPath launcherpath = appcertkitsdk.getPath(SakerWindowsUtils.SDK_WINDOWSAPPCERTKIT_PATH_APPXLAUNCHER);
		if (launcherpath == null) {
			throw new SDKPathNotFoundException("appxlauncher.exe not found in SDK: " + appcertkitsdk);
		}
		String entrypoint = getAppUserModelId(taskcontext, sdkrefs);

		SakerLog.log().verbose().println("Launching: " + entrypoint);

		SakerProcessBuilder pb = SakerProcessBuilder.create();
		pb.setCommand(ImmutableUtils.asUnmodifiableArrayList(launcherpath.toString(), entrypoint));
		pb.setStandardErrorMerge(true);
		CollectingProcessIOConsumer outconsumer = new CollectingProcessIOConsumer();
		pb.setStandardOutputConsumer(outconsumer);
		try (SakerProcess proc = pb.start()) {
			proc.processIO();
			//the exit code is the process id
			int exitcode = proc.waitFor();
			if (exitcode == 0) {
				throw new IOException("Failed to start application (" + entrypoint + "). Exit code: " + exitcode);
			}
		} finally {
			taskcontext.getStandardOut().write(outconsumer.getByteArrayRegion());
		}

		//rerun in the next build
		taskcontext.getTaskUtilities().getReportExecutionDependency(BuildTimeExecutionProperty.INSTANCE);

		return null;
	}

	protected abstract String getAppUserModelId(TaskContext taskcontext, NavigableMap<String, SDKReference> sdkrefs)
			throws Exception;

	@Override
	public Task<? extends Object> createTask(ExecutionContext executioncontext) {
		return this;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		SerialUtils.writeExternalMap(out, sdks);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		sdks = SerialUtils.readExternalSortedImmutableNavigableMap(in, SDKSupportUtils.getSDKNameComparator());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sdks == null) ? 0 : sdks.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LaunchAppxWorkerTaskFactory other = (LaunchAppxWorkerTaskFactory) obj;
		if (sdks == null) {
			if (other.sdks != null)
				return false;
		} else if (!sdks.equals(other.sdks))
			return false;
		return true;
	}
}
