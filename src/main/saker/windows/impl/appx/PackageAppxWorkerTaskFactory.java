package saker.windows.impl.appx;

import java.io.Externalizable;
import java.nio.file.Path;
import java.util.List;
import java.util.NavigableMap;

import saker.build.file.SakerDirectory;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.SakerPathFiles;
import saker.build.runtime.execution.SakerLog;
import saker.build.task.TaskContext;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.windows.api.appx.PackageAppxWorkerTaskOutput;
import saker.windows.main.appx.PackageAppxTaskFactory;

public class PackageAppxWorkerTaskFactory
		extends MakeAppxWithMappingsWorkerTaskFactoryBase<PackageAppxWorkerTaskOutput> {
	private static final long serialVersionUID = 1L;

	/**
	 * For {@link Externalizable}.
	 */
	public PackageAppxWorkerTaskFactory() {
	}

	public PackageAppxWorkerTaskFactory(NavigableMap<SakerPath, SakerPath> mappings) {
		super(mappings);
	}

	@Override
	public PackageAppxWorkerTaskOutput run(TaskContext taskcontext) throws Exception {
		taskcontext.setStandardOutDisplayIdentifier(PackageAppxTaskFactory.TASK_NAME);
		return super.run(taskcontext);
	}

	@Override
	protected List<String> getCommand(Path mappingsfilemirrorpath, Path outputfilepath, SakerPath makeappxpath) {
		return ImmutableUtils.asUnmodifiableArrayList(makeappxpath.toString(), "pack", "/o", "/f",
				mappingsfilemirrorpath.toString(), "/p", outputfilepath.toString());
	}

	@Override
	protected PackageAppxWorkerTaskOutputImpl getResult(SakerPath outputpath) {
		SakerLog.success().verbose().println("Created appx: " + outputpath);

		PackageAppxWorkerTaskOutputImpl result = new PackageAppxWorkerTaskOutputImpl(outputpath);
		return result;
	}

	@Override
	protected SakerDirectory getBaseOutputDirectory(TaskContext taskcontext) {
		return SakerPathFiles.requireBuildDirectory(taskcontext).getDirectoryCreate(PackageAppxTaskFactory.TASK_NAME);
	}

	@Override
	protected SakerPath getRelativeOutputPath(TaskContext taskcontext) {
		PackageAppxWorkerTaskIdentifier taskid = (PackageAppxWorkerTaskIdentifier) taskcontext.getTaskId();

		return taskid.getRelativeOutput();
	}

}
