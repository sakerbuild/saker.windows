package saker.windows.impl.appx;

import java.io.Externalizable;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.UUID;

import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.SakerLog;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.task.identifier.TaskIdentifier;
import saker.build.task.utils.StructuredTaskResult;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.build.trace.BuildTrace;
import saker.process.api.CollectingProcessIOConsumer;
import saker.process.api.SakerProcess;
import saker.process.api.SakerProcessBuilder;
import saker.std.api.util.SakerStandardUtils;
import saker.windows.impl.SakerWindowsImplUtils;
import saker.windows.main.appx.RegisterAppxTaskFactory;

public class RegisterAppxWorkerTaskFactory
		implements TaskFactory<Object>, Task<Object>, TaskIdentifier, Externalizable {
	private static final long serialVersionUID = 1L;

	private StructuredTaskResult appxManifestLocalSakerPathResult;
	private boolean allowReinstall;

	/**
	 * For {@link Externalizable}.
	 */
	public RegisterAppxWorkerTaskFactory() {
	}

	public RegisterAppxWorkerTaskFactory(StructuredTaskResult appxManifestLocalSakerPathResult) {
		this.appxManifestLocalSakerPathResult = appxManifestLocalSakerPathResult;
	}

	public void setAllowReinstall(boolean allowReinstall) {
		this.allowReinstall = allowReinstall;
	}

	@Override
	public Task<? extends Object> createTask(ExecutionContext executioncontext) {
		return this;
	}

	@Override
	public Object run(TaskContext taskcontext) throws Exception {
		if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
			BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_WORKER);
		}
		taskcontext.setStandardOutDisplayIdentifier(RegisterAppxTaskFactory.TASK_NAME);

		SakerPath appxlocalpath = (SakerPath) appxManifestLocalSakerPathResult.toResult(taskcontext);

		taskcontext.getTaskUtilities().getReportExecutionDependency(
				SakerStandardUtils.createLocalFileContentDescriptorExecutionProperty(appxlocalpath, UUID.randomUUID()));

		SakerProcessBuilder registerpb = SakerProcessBuilder.create();
		registerpb.setCommand(ImmutableUtils.asUnmodifiableArrayList("powershell", "-NoProfile", "-NonInteractive",
				"-Command", "Add-AppxPackage " + appxlocalpath.toString() + " -Register"));
		CollectingProcessIOConsumer registeroutconsumer = new CollectingProcessIOConsumer();
		CollectingProcessIOConsumer registererrconsumer = new CollectingProcessIOConsumer();
		registerpb.setStandardOutputConsumer(registeroutconsumer);
		registerpb.setStandardErrorConsumer(registererrconsumer);

		int ec;
		try (SakerProcess proc = registerpb.start()) {
			proc.processIO();
			ec = proc.waitFor();
			if (ec == 0) {
				//success.
				SakerLog.success().verbose().println("Registered appx at: " + appxlocalpath);
				return returnTaskResult(taskcontext, appxlocalpath);
			}
			String errstr = registererrconsumer.getOutputString();
			int index = errstr.indexOf("HRESULT:");
			String hres = errstr.substring(index + 8).trim();
			if (hres.startsWith("0x80073CFF")) {
				throw new IOException(
						"Error: Failed to register appx, make sure developer mode is enabled on this computer. "
								+ "(Settings/Update and Security/Developers)");
			} else if (hres.startsWith("0x80073CFB")) {
				if (!allowReinstall) {
					throw new IOException(
							"Appx is already registered in developement mode on this computer: " + appxlocalpath);
				}
				SakerLog.info().verbose().println("Appx is already registered. Reinstalling. (" + appxlocalpath + ")");
			} else {
				if (!allowReinstall) {
					throw new IOException("Unknown deployment error, see output for more info.");
				}
				//even though the deployment error is unrecognized, we reinstall the app
				//these issues can happen e.g. when the Application.Id attribute is modified
			}
		} catch (Throwable e) {
			try {
				SakerLog.error().verbose().println("Appx registration failed at: " + appxlocalpath);
				taskcontext.getStandardOut().write(registeroutconsumer.getByteArrayRegion());
				taskcontext.getStandardOut().write(registererrconsumer.getByteArrayRegion());
			} catch (Throwable e2) {
				e.addSuppressed(e2);
			}
			throw e;
		}
		//attempt removing and reinstalling
		//first determine the package full name

		String identityname;
		try (InputStream is = LocalFileProvider.getInstance().openInputStream(appxlocalpath)) {
			identityname = SakerWindowsImplUtils.getAppxManifestIdentityName(is);
		}
		if (ObjectUtils.isNullOrEmpty(identityname)) {
			throw new IllegalArgumentException(
					"Failed to determine Identity Name from AppxManifest.xml: " + appxlocalpath);
		}

		String packagefullname = SakerWindowsImplUtils.getAppxPackagePackageFullName(taskcontext, identityname);
		if (ObjectUtils.isNullOrEmpty(packagefullname)) {
			throw new IllegalStateException("Failed to determine appx package full name for: " + identityname);
		}

		SakerLog.info().verbose().println("Removing appx package with full name: " + packagefullname);
		SakerProcessBuilder removepb = SakerProcessBuilder.create();
		removepb.setCommand(ImmutableUtils.asUnmodifiableArrayList("powershell", "-NoProfile", "-NonInteractive",
				"-Command", "Remove-AppxPackage " + packagefullname));
		CollectingProcessIOConsumer removeoutconsumer = new CollectingProcessIOConsumer();
		CollectingProcessIOConsumer removeerrconsumer = new CollectingProcessIOConsumer();
		removepb.setStandardOutputConsumer(removeoutconsumer);
		removepb.setStandardErrorConsumer(removeerrconsumer);
		try (SakerProcess proc = removepb.start()) {
			proc.processIO();
			int removeec = proc.waitFor();
			if (removeec != 0) {
				throw new IOException(
						"Failed to remove appx package: " + packagefullname + " (Exit code: " + removeec + ")");
			}
		} catch (Throwable e) {
			try {
				taskcontext.getStandardOut().write(removeoutconsumer.getByteArrayRegion());
				taskcontext.getStandardOut().write(removeerrconsumer.getByteArrayRegion());
			} catch (Throwable e2) {
				e.addSuppressed(e2);
			}
			throw e;
		}

		//remove the old package, try to register it again
		CollectingProcessIOConsumer newregisteroutconsumer = new CollectingProcessIOConsumer();
		CollectingProcessIOConsumer newregistererrconsumer = new CollectingProcessIOConsumer();
		registerpb.setStandardOutputConsumer(newregisteroutconsumer);
		registerpb.setStandardErrorConsumer(newregistererrconsumer);
		try (SakerProcess proc = registerpb.start()) {
			proc.processIO();
			int newec = proc.waitFor();
			if (newec == 0) {
				//success.
				SakerLog.success().verbose().println("Registered appx at: " + appxlocalpath);
				return returnTaskResult(taskcontext, appxlocalpath);
			}
			throw new IOException("Failed to register appx after removing it: " + appxlocalpath);
		} finally {
			taskcontext.getStandardOut().write(newregisteroutconsumer.getByteArrayRegion());
			taskcontext.getStandardOut().write(newregistererrconsumer.getByteArrayRegion());
		}
	}

	private static Object returnTaskResult(TaskContext taskcontext, SakerPath appxlocalpath) {
		RegisterAppxWorkerTaskOutputImpl result = new RegisterAppxWorkerTaskOutputImpl(appxlocalpath);
		taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
		return result;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(appxManifestLocalSakerPathResult);
		out.writeBoolean(allowReinstall);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		appxManifestLocalSakerPathResult = SerialUtils.readExternalObject(in);
		allowReinstall = in.readBoolean();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (allowReinstall ? 1231 : 1237);
		result = prime * result
				+ ((appxManifestLocalSakerPathResult == null) ? 0 : appxManifestLocalSakerPathResult.hashCode());
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
		RegisterAppxWorkerTaskFactory other = (RegisterAppxWorkerTaskFactory) obj;
		if (allowReinstall != other.allowReinstall)
			return false;
		if (appxManifestLocalSakerPathResult == null) {
			if (other.appxManifestLocalSakerPathResult != null)
				return false;
		} else if (!appxManifestLocalSakerPathResult.equals(other.appxManifestLocalSakerPathResult))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "RegisterAppxWorkerTaskFactory[" + (appxManifestLocalSakerPathResult != null
				? "appxManifestLocalSakerPathResult=" + appxManifestLocalSakerPathResult + ", "
				: "") + "allowReinstall=" + allowReinstall + "]";
	}

}
