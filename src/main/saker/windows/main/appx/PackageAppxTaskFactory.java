package saker.windows.main.appx;

import java.util.Map;
import java.util.NavigableMap;

import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.utils.SimpleStructuredObjectTaskResult;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.trace.BuildTrace;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.utils.FrontendTaskFactory;
import saker.sdk.support.api.SDKDescription;
import saker.sdk.support.main.SDKSupportFrontendUtils;
import saker.sdk.support.main.option.SDKDescriptionTaskOption;
import saker.std.main.file.utils.TaskOptionUtils;
import saker.windows.api.appx.PrepareAppxWorkerTaskOutput;
import saker.windows.impl.appx.PackageAppxWorkerTaskFactory;
import saker.windows.impl.appx.PackageAppxWorkerTaskIdentifier;
import saker.windows.main.TaskDocs;
import saker.windows.main.TaskDocs.DocPackageAppxWorkerTaskOutput;

@NestTaskInformation(returnType = @NestTypeUsage(DocPackageAppxWorkerTaskOutput.class))
@NestInformation("Creates an .appx package from the prepared application directory.\n"
		+ "The task creates an .appx archive from the prepared application contents. It expects the " + "output of the "
		+ PrepareAppxTaskFactory.TASK_NAME + "() task as its input.\n"
		+ "The task uses the makeappx tool to perform its operations.")

@NestParameterInformation(value = "Input",
		aliases = "",
		required = true,
		type = @NestTypeUsage(PackageAppxTaskFactory.AppxInputTaskOption.class),
		info = @NestInformation("Specifies the contents of the .appx package.\n"
				+ "The parameter expects the output of the " + PrepareAppxTaskFactory.TASK_NAME
				+ "() task as the input."))
@NestParameterInformation(value = "Output",
		type = @NestTypeUsage(SakerPath.class),
		info = @NestInformation("A forward relative output path that specifies the output location of the .appx bundle.\n"
				+ "It can be used to have a better output location than the automatically generated one."))
@NestParameterInformation(value = "SDKs",
		type = @NestTypeUsage(value = Map.class,
				elementTypes = { saker.sdk.support.main.TaskDocs.DocSdkNameOption.class,
						SDKDescriptionTaskOption.class }),
		info = @NestInformation(TaskDocs.SDKS))
public class PackageAppxTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "saker.appx.package";

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Object>() {

			@SakerInput(value = { "", "Input", }, required = true)
			public AppxInputTaskOption inputOption;

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
				NavigableMap<SakerPath, SakerPath> mappings = inputOption.getMappings(taskcontext);
				SakerPath outputpath;
				if (outputOption != null) {
					TaskOptionUtils.requireForwardRelativePathWithFileName(outputOption, "Output");
					outputpath = SakerPath.valueOf(TASK_NAME).resolve(outputOption);
				} else {
					outputpath = SakerPath.valueOf(TASK_NAME).resolve(inputOption.inferRelativeOutput(taskcontext));
				}

				PackageAppxWorkerTaskIdentifier workertaskid = new PackageAppxWorkerTaskIdentifier(outputpath);
				PackageAppxWorkerTaskFactory workertask = new PackageAppxWorkerTaskFactory(mappings);
				workertask.setSdks(sdks);
				taskcontext.startTask(workertaskid, workertask, null);

				SimpleStructuredObjectTaskResult result = new SimpleStructuredObjectTaskResult(workertaskid);
				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
				return result;
			}
		};
	}

	@NestInformation("Input .appx contents.\n" + "The option expects the output of the "
			+ PrepareAppxTaskFactory.TASK_NAME + "() task.")
	public static abstract class AppxInputTaskOption {
		public abstract NavigableMap<SakerPath, SakerPath> getMappings(TaskContext taskcontext);

		public abstract SakerPath inferRelativeOutput(TaskContext taskcontext);

		public static AppxInputTaskOption valueOf(PrepareAppxWorkerTaskOutput input) {
			return new AppxInputTaskOption() {
				@Override
				public NavigableMap<SakerPath, SakerPath> getMappings(TaskContext taskcontext) {
					return input.getMappings();
				}

				@Override
				public SakerPath inferRelativeOutput(TaskContext taskcontext) {
					SakerPath dirpath = input.getAppxDirectory();
					int idx = dirpath.indexOfName(PrepareAppxTaskFactory.TASK_NAME);
					if (idx < 0) {
						//dont know, use something
						return SakerPath.valueOf(dirpath.getFileName() + ".appx");
					}
					SakerPath res = dirpath.subPath(idx + 1);
					if (SakerPath.EMPTY.equals(res)) {
						//shouldn't really happen, but check anyway
						return SakerPath.valueOf(PrepareAppxTaskFactory.TASK_NAME + ".appx");
					}
					//join the directory names with underscore
					//so the .appx won't be named something like x64.appx
					//but like MyAppIdentity.Name_1.2.3_x64.appx
					return SakerPath.valueOf(StringUtils.toStringJoin(null, "_", res.nameIterator(), ".appx"));
				}
			};
		}
	}
}
