package saker.windows.main.signtool;

import java.util.Map;
import java.util.NavigableMap;

import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.utils.SimpleStructuredObjectTaskResult;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.trace.BuildTrace;
import saker.nest.utils.FrontendTaskFactory;
import saker.sdk.support.api.SDKDescription;
import saker.sdk.support.main.SDKSupportFrontendUtils;
import saker.sdk.support.main.option.SDKDescriptionTaskOption;
import saker.std.api.file.location.ExecutionFileLocation;
import saker.std.api.file.location.FileLocation;
import saker.std.api.util.SakerStandardUtils;
import saker.std.main.file.option.FileLocationTaskOption;
import saker.std.main.file.utils.TaskOptionUtils;
import saker.windows.api.appx.BundleAppxWorkerTaskOutput;
import saker.windows.api.appx.PackageAppxWorkerTaskOutput;
import saker.windows.impl.signtool.SigntoolSignWorkerTaskFactory;
import saker.windows.impl.signtool.SigntoolSignWorkerTaskIdentifier;

public class SigntoolSignTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "saker.windows.signtool.sign";

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Object>() {

			@SakerInput(value = { "", "Input" }, required = true)
			public SignInputTaskOption inputOption;

			@SakerInput(value = { "Certificate" })
			public FileLocationTaskOption certificateOption;
			@SakerInput(value = { "Password" })
			public String passwordOption;
			@SakerInput(value = { "Algorithm" })
			public String algorithmOption;

			@SakerInput(value = { "SDKs" })
			public Map<String, SDKDescriptionTaskOption> sdksOption;

			@SakerInput(value = "Output")
			public SakerPath outputOption;

			@Override
			public Object run(TaskContext taskcontext) throws Exception {
				if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
					BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_FRONTEND);
				}

				NavigableMap<String, SDKDescription> sdks = SDKSupportFrontendUtils.toSDKDescriptionMap(sdksOption);
				FileLocation inputfl = inputOption.toFileLocation(taskcontext);
				FileLocation certfl = TaskOptionUtils.toFileLocation(certificateOption, taskcontext);

				SakerPath outputpath;
				if (outputOption != null) {
					TaskOptionUtils.requireForwardRelativePathWithFileName(outputOption, "Output");
					outputpath = SakerPath.valueOf(TASK_NAME).resolve(outputOption);
				} else {
					outputpath = SakerPath.valueOf(SakerStandardUtils.getFileLocationFileName(inputfl));
				}

				SigntoolSignWorkerTaskIdentifier workertaskid = new SigntoolSignWorkerTaskIdentifier(outputpath);
				SigntoolSignWorkerTaskFactory workertask = new SigntoolSignWorkerTaskFactory(inputfl);
				workertask.setSdks(sdks);
				workertask.setCertificate(certfl);
				workertask.setPassword(passwordOption);
				workertask.setAlgorithm(algorithmOption);
				taskcontext.startTask(workertaskid, workertask, null);

				SimpleStructuredObjectTaskResult result = new SimpleStructuredObjectTaskResult(workertaskid);
				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
				return result;
			}
		};
	}

	public static abstract class SignInputTaskOption {
		public abstract FileLocation toFileLocation(TaskContext taskcontext);

		public static SignInputTaskOption valueOf(String input) {
			return valueOf(SakerPath.valueOf(input));
		}

		public static SignInputTaskOption valueOf(SakerPath input) {
			return new SignInputTaskOption() {

				@Override
				public FileLocation toFileLocation(TaskContext taskcontext) {
					if (input.isRelative()) {
						return ExecutionFileLocation.create(taskcontext.getTaskWorkingDirectoryPath().resolve(input));
					}
					return ExecutionFileLocation.create(input);
				}
			};
		}

		public static SignInputTaskOption valueOf(FileLocation input) {
			return new SignInputTaskOption() {
				@Override
				public FileLocation toFileLocation(TaskContext taskcontext) {
					return input;
				}
			};
		}

		public static SignInputTaskOption valueOf(PackageAppxWorkerTaskOutput input) {
			return valueOf(ExecutionFileLocation.create(input.getPath()));
		}

		public static SignInputTaskOption valueOf(BundleAppxWorkerTaskOutput input) {
			return valueOf(ExecutionFileLocation.create(input.getPath()));
		}
	}
}
