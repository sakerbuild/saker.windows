package saker.windows.main.appx;

import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.util.Collection;
import java.util.NavigableMap;
import java.util.StringJoiner;
import java.util.TreeMap;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import saker.build.file.SakerFile;
import saker.build.file.content.ContentDescriptor;
import saker.build.file.content.DirectoryContentDescriptor;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.CommonTaskContentDescriptors;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.exception.MissingRequiredParameterException;
import saker.build.task.utils.SimpleStructuredObjectTaskResult;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.trace.BuildTrace;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.utils.FrontendTaskFactory;
import saker.std.api.file.location.ExecutionFileLocation;
import saker.std.api.file.location.FileLocation;
import saker.std.api.file.location.FileLocationVisitor;
import saker.std.api.file.location.LocalFileLocation;
import saker.std.api.util.SakerStandardUtils;
import saker.std.main.dir.prepare.RelativeContentsTaskOption;
import saker.std.main.file.option.FileLocationTaskOption;
import saker.std.main.file.utils.TaskOptionUtils;
import saker.windows.impl.appx.PrepareAppxWorkerTaskFactory;
import saker.windows.impl.appx.PrepareAppxWorkerTaskIdentifier;
import saker.windows.main.TaskDocs.DocPrepareAppxWorkerTaskOutput;

@NestTaskInformation(returnType = @NestTypeUsage(DocPrepareAppxWorkerTaskOutput.class))
@NestInformation("Prepares appx application contents into an output directory.\n"
		+ "The task is used to set up the content hierarchy for an application.\n"
		+ "The task requires an AppxManifest.xml to be specified in the root directory.")

@NestParameterInformation(value = "AppxManifest",
		type = @NestTypeUsage(FileLocationTaskOption.class),
		info = @NestInformation("Specifies the AppxManifest.xml for the application.\n"
				+ "The file set for this parameter will be placed into the application directory "
				+ "with the AppxManifest.xml name.\n"
				+ "You don't need to use this parameter if you already specify an AppxManifest.xml using "
				+ "the Contents parameter."))
@NestParameterInformation(value = "Contents",
		type = @NestTypeUsage(value = Collection.class, elementTypes = { RelativeContentsTaskOption.class }),
		info = @NestInformation("Specifies the file contents of the application.\n"
				+ "All file contents of the application should be specified for this parameter."))
@NestParameterInformation(value = "Output",
		type = @NestTypeUsage(SakerPath.class),
		info = @NestInformation("A forward relative output path that specifies the output location of the application contents.\n"
				+ "It can be used to have a better output location than the automatically generated one."))
public class PrepareAppxTaskFactory extends FrontendTaskFactory<Object> {
	private static final SakerPath PATH_APPXMANIFESTXML = SakerPath.valueOf("AppxManifest.xml");
	private static final Object DEP_TAG_APPXMANIFESTXML_CONTENTS = PATH_APPXMANIFESTXML;

	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "saker.appx.prepare";

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Object>() {

			@SakerInput(value = "AppxManifest")
			public FileLocationTaskOption appxManifestOption;

			@SakerInput(value = "Contents")
			public Collection<RelativeContentsTaskOption> contentsOption;

			@SakerInput(value = "Output")
			public SakerPath outputOption;

			@Override
			public Object run(TaskContext taskcontext) throws Exception {
				if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
					BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_FRONTEND);
				}

				FileLocation appxfl = TaskOptionUtils.toFileLocation(appxManifestOption, taskcontext);
				contentsOption = ObjectUtils.cloneArrayList(contentsOption, RelativeContentsTaskOption::clone);
				NavigableMap<SakerPath, FileLocation> resources = RelativeContentsTaskOption.toInputMap(taskcontext,
						contentsOption, null);
				if (resources == null) {
					resources = new TreeMap<>();
				}
				if (appxfl == null) {
					appxfl = ObjectUtils.getMapValue(resources, PATH_APPXMANIFESTXML);
					if (appxfl == null) {
						taskcontext.abortExecution(new MissingRequiredParameterException(
								"No AppxManifest.xml specified.", taskcontext.getTaskId()));
						return null;
					}
				} else {
					FileLocation prevappx = resources.put(PATH_APPXMANIFESTXML, appxfl);
					if (prevappx != null && !prevappx.equals(appxfl)) {
						taskcontext.abortExecution(new IllegalArgumentException(
								"Multiple AppxManifest.xml files specified: " + prevappx + " and " + appxfl));
						return null;
					}
				}

				SakerPath outputpath;
				if (outputOption != null) {
					TaskOptionUtils.requireForwardRelativePathWithFileName(outputOption, "Output");
					outputpath = SakerPath.valueOf(TASK_NAME).resolve(outputOption);
				} else {
					outputpath = SakerPath.valueOf(TASK_NAME)
							.resolve(inferOutputPathFromAppxManifest(taskcontext, appxfl));
				}

				PrepareAppxWorkerTaskFactory workertask = new PrepareAppxWorkerTaskFactory(resources);
				PrepareAppxWorkerTaskIdentifier workertaskid = new PrepareAppxWorkerTaskIdentifier(outputpath);
				taskcontext.startTask(workertaskid, workertask, null);

				SimpleStructuredObjectTaskResult result = new SimpleStructuredObjectTaskResult(workertaskid);
				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
				return result;
			}
		};
	}

	protected static SakerPath inferOutputPathFromAppxManifest(TaskContext taskcontext, FileLocation fl)
			throws Exception {
		SakerPath[] result = { null };

		fl.accept(new FileLocationVisitor() {
			@Override
			public void visit(LocalFileLocation loc) {
				SakerPath path = loc.getLocalPath();
				ContentDescriptor cd = taskcontext.getTaskUtilities().getReportExecutionDependency(
						SakerStandardUtils.createLocalFileContentDescriptorExecutionProperty(path, UUID.randomUUID()));
				if (cd == null || cd instanceof DirectoryContentDescriptor) {
					throw ObjectUtils
							.sneakyThrow(new NoSuchFileException("Specified AppxManifest is not a file: " + path));
				}
				try (InputStream is = LocalFileProvider.getInstance().openInputStream(path)) {
					result[0] = inferOutputPathFromAppxManifestStream(is);
				} catch (Exception e) {
					throw ObjectUtils.sneakyThrow(e);
				}
			}

			@Override
			public void visit(ExecutionFileLocation loc) {
				SakerPath path = loc.getPath();
				SakerFile f = taskcontext.getTaskUtilities().resolveFileAtPath(path);
				if (f == null) {
					taskcontext.reportInputFileDependency(DEP_TAG_APPXMANIFESTXML_CONTENTS, path,
							CommonTaskContentDescriptors.IS_NOT_FILE);
					throw ObjectUtils
							.sneakyThrow(new NoSuchFileException("Specified AppxManifest is not a file: " + path));
				}
				taskcontext.reportInputFileDependency(DEP_TAG_APPXMANIFESTXML_CONTENTS, path, f.getContentDescriptor());
				try (InputStream is = f.openInputStream()) {
					result[0] = inferOutputPathFromAppxManifestStream(is);
				} catch (Exception e) {
					throw ObjectUtils.sneakyThrow(e);
				}
			}
		});
		return result[0];
	}

	protected static SakerPath inferOutputPathFromAppxManifestStream(InputStream is) throws Exception {
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		//not namespace aware
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(is);
		Element rootelem = doc.getDocumentElement();
		if (!"Package".equals(rootelem.getNodeName())) {
			throw new IllegalArgumentException(
					"Invalid AppxManifest.xml, expected Package root element instead of " + rootelem.getNodeName());
		}
		NodeList children = rootelem.getChildNodes();
		for (int i = 0, clen = children.getLength(); i < clen; i++) {
			Node item = children.item(i);
			if (item.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}
			if (!"Identity".equals(item.getNodeName())) {
				continue;
			}
			Element elem = (Element) item;
			String name = elem.getAttribute("Name");
			String version = elem.getAttribute("Version");
			String arch = elem.getAttribute("ProcessorArchitecture");
			StringJoiner joiner = new StringJoiner("_");
			if (!ObjectUtils.isNullOrEmpty(name)) {
				joiner.add(name);
			}
			if (!ObjectUtils.isNullOrEmpty(version)) {
				joiner.add(version);
			}
			if (!ObjectUtils.isNullOrEmpty(arch)) {
				joiner.add(arch);
			}
			String joined = joiner.toString();
			if (ObjectUtils.isNullOrEmpty(joined)) {
				joined = "default";
			}
			return SakerPath.valueOf(joined);
		}
		throw new IllegalArgumentException("Identity element not found in AppxManifest.");
	}
}
