package saker.windows.impl.appx;

import java.io.Externalizable;
import java.nio.file.Path;
import java.util.List;
import java.util.NavigableMap;

import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.SakerLog;
import saker.build.task.TaskContext;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.windows.api.appx.BundleAppxWorkerTaskOutput;
import saker.windows.main.appx.BundleAppxTaskFactory;

public class BundleAppxWorkerTaskFactory extends MakeAppxWithMappingsWorkerTaskFactoryBase<BundleAppxWorkerTaskOutput> {
	private static final long serialVersionUID = 1L;

	/**
	 * For {@link Externalizable}.
	 */
	public BundleAppxWorkerTaskFactory() {
	}

	public BundleAppxWorkerTaskFactory(NavigableMap<SakerPath, SakerPath> mappings) {
		super(mappings);
	}

	@Override
	public BundleAppxWorkerTaskOutput run(TaskContext taskcontext) throws Exception {
		taskcontext.setStandardOutDisplayIdentifier(BundleAppxTaskFactory.TASK_NAME);
		return super.run(taskcontext);
	}

	@Override
	protected List<String> getCommand(Path mappingsfilemirrorpath, Path outputfilepath, SakerPath makeappxpath) {
		return ImmutableUtils.asUnmodifiableArrayList(makeappxpath.toString(), "bundle", "/o", "/f",
				mappingsfilemirrorpath.toString(), "/p", outputfilepath.toString());
	}

	@Override
	protected BundleAppxWorkerTaskOutput getResult(SakerPath outputpath) {
		SakerLog.success().verbose().println("Created appxbundle: " + outputpath);

		return new BundleAppxWorkerTaskOutputImpl(outputpath);
	}

	@Override
	protected SakerPath getRelativeOutputPath(TaskContext taskcontext) {
		BundleAppxWorkerTaskIdentifier taskid = (BundleAppxWorkerTaskIdentifier) taskcontext.getTaskId();

		return taskid.getRelativeOutput();
	}

}
