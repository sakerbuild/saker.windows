package saker.windows.main.appx.manifest;

import java.util.Set;

import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.SakerLog;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.utils.SimpleStructuredObjectTaskResult;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.trace.BuildTrace;
import saker.nest.utils.FrontendTaskFactory;
import saker.std.api.file.location.FileLocation;
import saker.std.main.file.option.FileLocationTaskOption;
import saker.std.main.file.utils.TaskOptionUtils;
import saker.windows.impl.appx.manifest.PatchAppxManifestWorkerTaskFactory;
import saker.windows.impl.appx.manifest.PatchAppxManifestWorkerTaskIdentifier;

public class PatchAppxManifestTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "saker.appx.manifest.patch";

	public static final Set<String> KNOWN_ARCHITECTURES = ImmutableUtils
			.makeImmutableNavigableSet(new String[] { "x86", "x64", "arm", "arm64", "neutral" });

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Object>() {

			@SakerInput(value = { "", "Manifest", "AppxManifest" }, required = true)
			public FileLocationTaskOption manifestOption;

			@SakerInput(value = "ProcessorArchitecture")
			public String processorArchitectureOption;
			@SakerInput(value = "Version")
			public String versionOption;

			@SakerInput(value = "Output")
			public SakerPath outputOption;

			@Override
			public Object run(TaskContext taskcontext) throws Exception {
				if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
					BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_FRONTEND);
				}

				FileLocation file = TaskOptionUtils.toFileLocation(manifestOption, taskcontext);

				SakerPath outputpath;
				if (outputOption != null) {
					TaskOptionUtils.requireForwardRelativePathWithFileName(outputOption, "Output");
					outputpath = SakerPath.valueOf(TASK_NAME).resolve(outputOption);
				} else {
					outputpath = SakerPath.valueOf(TASK_NAME);
					if (processorArchitectureOption != null) {
						outputpath.resolve(processorArchitectureOption);
					}
					if (versionOption != null) {
						outputpath.resolve(versionOption);
					}
					outputpath = outputpath.resolve("AppxManifest.xml");
				}

				if (processorArchitectureOption != null && !KNOWN_ARCHITECTURES.contains(processorArchitectureOption)) {
					SakerLog.warning().taskScriptPosition(taskcontext)
							.println("Unrecognized processor architecture value: " + processorArchitectureOption
									+ ". Possible values: " + StringUtils.toStringJoin(", ", KNOWN_ARCHITECTURES));
				}

				PatchAppxManifestWorkerTaskIdentifier workertaskid = new PatchAppxManifestWorkerTaskIdentifier(
						outputpath);
				PatchAppxManifestWorkerTaskFactory workertask = new PatchAppxManifestWorkerTaskFactory(file);
				workertask.setProcessorArchitecture(processorArchitectureOption);
				workertask.setVersion(versionOption);

				taskcontext.startTask(workertaskid, workertask, null);

				SimpleStructuredObjectTaskResult result = new SimpleStructuredObjectTaskResult(workertaskid);
				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
				return result;
			}
		};
	}

}
