package saker.windows.impl.signtool;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableMap;
import java.util.UUID;

import saker.build.file.SakerDirectory;
import saker.build.file.SakerFile;
import saker.build.file.content.ContentDescriptor;
import saker.build.file.content.DirectoryContentDescriptor;
import saker.build.file.path.ProviderHolderPathKey;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.file.provider.SakerPathFiles;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.SakerLog;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskExecutionUtilities;
import saker.build.task.TaskExecutionUtilities.MirroredFileContents;
import saker.build.task.TaskFactory;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.ArrayUtils;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.build.trace.BuildTrace;
import saker.process.api.CollectingProcessIOConsumer;
import saker.process.api.SakerProcess;
import saker.process.api.SakerProcessBuilder;
import saker.sdk.support.api.SDKDescription;
import saker.sdk.support.api.SDKReference;
import saker.sdk.support.api.SDKSupportUtils;
import saker.sdk.support.api.exc.SDKPathNotFoundException;
import saker.std.api.file.location.ExecutionFileLocation;
import saker.std.api.file.location.FileLocation;
import saker.std.api.file.location.FileLocationVisitor;
import saker.std.api.file.location.LocalFileLocation;
import saker.std.api.util.SakerStandardUtils;
import saker.windows.api.SakerWindowsUtils;
import saker.windows.api.signtool.SigntoolSignWorkerTaskOutput;
import saker.windows.main.signtool.SigntoolSignTaskFactory;

public class SigntoolSignWorkerTaskFactory
		implements TaskFactory<SigntoolSignWorkerTaskOutput>, Task<SigntoolSignWorkerTaskOutput>, Externalizable {
	private static final long serialVersionUID = 1L;

	private FileLocation input;
	/**
	 * /f
	 */
	private FileLocation certificate;
	/**
	 * /fd
	 */
	private String algorithm;
	/**
	 * /p
	 */
	private String password;

	private NavigableMap<String, SDKDescription> sdks;

	/**
	 * For {@link Externalizable}.
	 */
	public SigntoolSignWorkerTaskFactory() {
	}

	public SigntoolSignWorkerTaskFactory(FileLocation input) {
		this.input = input;
	}

	public void setSdks(NavigableMap<String, SDKDescription> sdks) {
		this.sdks = ImmutableUtils.makeImmutableNavigableMap(sdks);
	}

	public void setCertificate(FileLocation certificate) {
		this.certificate = certificate;
	}

	public void setAlgorithm(String algorithm) {
		this.algorithm = algorithm;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Override
	public SigntoolSignWorkerTaskOutput run(TaskContext taskcontext) throws Exception {
		if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
			BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_WORKER);
		}
		taskcontext.setStandardOutDisplayIdentifier(SigntoolSignTaskFactory.TASK_NAME);

		SigntoolSignWorkerTaskIdentifier taskid = (SigntoolSignWorkerTaskIdentifier) taskcontext.getTaskId();
		SakerPath relativeoutputpath = taskid.getRelativeOutput();
		TaskExecutionUtilities taskutils = taskcontext.getTaskUtilities();
		SakerDirectory outputdir = taskutils.resolveDirectoryAtRelativePathCreate(
				SakerPathFiles.requireBuildDirectory(taskcontext).getDirectoryCreate(SigntoolSignTaskFactory.TASK_NAME),
				relativeoutputpath.getParent());

		LocalFileProvider localfp = LocalFileProvider.getInstance();
		//signtool overwrites the input so the actual input needs to be copied to the output path
		String outputfilename = relativeoutputpath.getFileName();
		Path inputpath = taskcontext.getExecutionContext()
				.toMirrorPath(outputdir.getSakerPath().resolve(outputfilename));
		ProviderHolderPathKey inputpathkey = localfp.getPathKey(inputpath);
		input.accept(new FileLocationVisitor() {
			@Override
			public void visit(LocalFileLocation loc) {
				SakerPath localpath = loc.getLocalPath();
				ContentDescriptor cd = taskcontext.getTaskUtilities().getReportExecutionDependency(SakerStandardUtils
						.createLocalFileContentDescriptorExecutionProperty(localpath, UUID.randomUUID()));
				if (cd == null || cd instanceof DirectoryContentDescriptor) {
					throw ObjectUtils.sneakyThrow(new NoSuchFileException("Not a file: " + localpath));
				}
				try {
					//copy the input to the output path
					taskutils.synchronize(localfp.getPathKey(localpath), inputpathkey,
							TaskExecutionUtilities.SYNCHRONIZE_FLAG_DELETE_INTERMEDIATE_FILES);
				} catch (Exception e) {
					throw ObjectUtils.sneakyThrow(e);
				}
			}

			@Override
			public void visit(ExecutionFileLocation loc) {
				SakerPath path = loc.getPath();
				SakerFile f = taskutils.resolveFileAtPath(path);
				if (f == null || f instanceof SakerDirectory) {
					throw ObjectUtils.sneakyThrow(new NoSuchFileException("Not a file: " + path));
				}
				try {
					f.synchronize(inputpathkey);
					taskcontext.reportInputFileDependency(null, path, f.getContentDescriptor());
				} catch (NullPointerException | IOException e) {
					throw ObjectUtils.sneakyThrow(e);
				}
			}
		});
		Path certpath = getPathOfFile(taskcontext, certificate);

		SakerPath signtoolexe = getSigntoolExecutablePath(taskcontext);

		List<String> cmd = new ArrayList<>();
		cmd.add(signtoolexe.toString());
		cmd.add("sign");
		if (!ObjectUtils.isNullOrEmpty(algorithm)) {
			cmd.add("/fd");
			cmd.add(algorithm);
		}
		if (certpath != null) {
			cmd.add("/f");
			cmd.add(certpath.toString());
		}
		if (!ObjectUtils.isNullOrEmpty(password)) {
			cmd.add("/p");
			cmd.add(password);
		}
		if (inputpath != null) {
			cmd.add(inputpath.toString());
		}

		SakerProcessBuilder pb = SakerProcessBuilder.create();
		pb.setCommand(cmd);
		pb.setStandardErrorMerge(true);
		CollectingProcessIOConsumer outconsumer = new CollectingProcessIOConsumer();
		pb.setStandardOutputConsumer(outconsumer);
		boolean printout = false;
		try (SakerProcess proc = pb.start()) {
			proc.processIO();
			int ec = proc.waitFor();
			if (ec != 0) {
				if (ec == 2) {
					//as per documentation, this means warnings.
					//don't throw an exception
					printout = true;
					SakerLog.warning().verbose().println("Signing finished with warnings: " + relativeoutputpath);
				} else {
					throw new IOException("Failed to run signtool. Exit code: " + ec);
				}
			} else {
				SakerLog.success().verbose().println("Signing completed: " + relativeoutputpath);
			}
		} catch (Throwable e) {
			printout = true;
			throw e;
		} finally {
			if (printout) {
				taskcontext.getStandardOut().write(outconsumer.getByteArrayRegion());
			}
		}

		taskutils.addSynchronizeInvalidatedProviderPathFileToDirectory(outputdir, inputpathkey, outputfilename);
		SakerFile outfile = outputdir.get(outputfilename);
		SakerPath outputsakerpath = outfile.getSakerPath();
		taskcontext.reportOutputFileDependency(null, outputsakerpath, outfile.getContentDescriptor());

		SigntoolSignWorkerTaskOutputImpl result = new SigntoolSignWorkerTaskOutputImpl(outputsakerpath);
		taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
		return result;
	}

	private static Path getPathOfFile(TaskContext taskcontext, FileLocation fl) throws Exception {
		if (fl == null) {
			return null;
		}
		Path[] result = { null };
		fl.accept(new FileLocationVisitor() {
			@Override
			public void visit(LocalFileLocation loc) {
				result[0] = LocalFileProvider.toRealPath(loc.getLocalPath());
				ContentDescriptor cd = taskcontext.getTaskUtilities().getReportExecutionDependency(SakerStandardUtils
						.createLocalFileContentDescriptorExecutionProperty(loc.getLocalPath(), UUID.randomUUID()));
				if (cd == null || cd instanceof DirectoryContentDescriptor) {
					throw ObjectUtils.sneakyThrow(new NoSuchFileException("Not a file: " + loc.getLocalPath()));
				}
			}

			@Override
			public void visit(ExecutionFileLocation loc) {
				try {
					MirroredFileContents mirroredcontents = taskcontext.getTaskUtilities()
							.mirrorFileAtPathContents(loc.getPath());
					result[0] = mirroredcontents.getPath();
					taskcontext.reportInputFileDependency(null, loc.getPath(), mirroredcontents.getContents());
				} catch (NullPointerException | IOException e) {
					throw ObjectUtils.sneakyThrow(e);
				}
			}

		});
		return result[0];
	}

	@Override
	public Task<? extends SigntoolSignWorkerTaskOutput> createTask(ExecutionContext executioncontext) {
		return this;
	}

	private SakerPath getSigntoolExecutablePath(TaskContext taskcontext) throws Exception {
		Throwable[] causes = {};
		boolean haswk = sdks.containsKey(SakerWindowsUtils.SDK_NAME_WINDOWSKITS);
		if (haswk) {
			try {
				SDKReference sdk = SDKSupportUtils.requireSDK(SDKSupportUtils.resolveSDKReferences(taskcontext, sdks),
						SakerWindowsUtils.SDK_NAME_WINDOWSKITS);
				return tryGetExecutablePathFromSDK(sdk, SakerWindowsUtils.SDK_WINDOWSKITS_PATH_SIGNTOOL_X86);
			} catch (Exception e) {
				causes = ArrayUtils.appended(causes, e);
			}
		}
		boolean hasappcertkit = sdks.containsKey(SakerWindowsUtils.SDK_NAME_WINDOWSAPPCERTKIT);
		if (hasappcertkit) {
			try {
				SDKReference sdk = SDKSupportUtils.requireSDK(SDKSupportUtils.resolveSDKReferences(taskcontext, sdks),
						SakerWindowsUtils.SDK_NAME_WINDOWSAPPCERTKIT);
				return tryGetExecutablePathFromSDK(sdk, SakerWindowsUtils.SDK_WINDOWSAPPCERTKIT_PATH_SIGNTOOL);
			} catch (Exception e) {
				causes = ArrayUtils.appended(causes, e);
			}
		}
		if (!haswk) {
			try {
				SDKReference sdk = SDKSupportUtils.resolveSDKReference(taskcontext,
						SakerWindowsUtils.getDefaultWindowsKitsSDK());
				return tryGetExecutablePathFromSDK(sdk, SakerWindowsUtils.SDK_WINDOWSKITS_PATH_SIGNTOOL_X86);
			} catch (Exception e) {
				causes = ArrayUtils.appended(causes, e);
			}
		}
		if (!hasappcertkit) {
			try {
				SDKReference sdk = SDKSupportUtils.resolveSDKReference(taskcontext,
						SakerWindowsUtils.getDefaultWindowsAppCertKitSDK());
				return tryGetExecutablePathFromSDK(sdk, SakerWindowsUtils.SDK_WINDOWSAPPCERTKIT_PATH_SIGNTOOL);
			} catch (Exception e) {
				causes = ArrayUtils.appended(causes, e);
			}
		}
		SDKPathNotFoundException exc = new SDKPathNotFoundException("signtool.exe not found in SDKs.");
		for (Throwable c : causes) {
			exc.addSuppressed(c);
		}
		throw exc;
	}

	private static SakerPath tryGetExecutablePathFromSDK(SDKReference sdk, String pathid)
			throws Exception, IOException {
		SakerPath makeappxpath = sdk.getPath(pathid);
		if (makeappxpath != null && LocalFileProvider.getInstance().getFileAttributes(makeappxpath).isRegularFile()) {
			return makeappxpath;
		}
		throw new SDKPathNotFoundException("Executable not found in SDK: " + sdk + " with path identifier: " + pathid);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		SerialUtils.writeExternalMap(out, sdks);
		out.writeObject(input);
		out.writeObject(certificate);
		out.writeObject(algorithm);
		out.writeObject(password);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		sdks = SerialUtils.readExternalSortedImmutableNavigableMap(in, SDKSupportUtils.getSDKNameComparator());
		input = SerialUtils.readExternalObject(in);
		certificate = SerialUtils.readExternalObject(in);
		algorithm = SerialUtils.readExternalObject(in);
		password = SerialUtils.readExternalObject(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((algorithm == null) ? 0 : algorithm.hashCode());
		result = prime * result + ((certificate == null) ? 0 : certificate.hashCode());
		result = prime * result + ((input == null) ? 0 : input.hashCode());
		result = prime * result + ((password == null) ? 0 : password.hashCode());
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
		SigntoolSignWorkerTaskFactory other = (SigntoolSignWorkerTaskFactory) obj;
		if (algorithm == null) {
			if (other.algorithm != null)
				return false;
		} else if (!algorithm.equals(other.algorithm))
			return false;
		if (certificate == null) {
			if (other.certificate != null)
				return false;
		} else if (!certificate.equals(other.certificate))
			return false;
		if (input == null) {
			if (other.input != null)
				return false;
		} else if (!input.equals(other.input))
			return false;
		if (password == null) {
			if (other.password != null)
				return false;
		} else if (!password.equals(other.password))
			return false;
		if (sdks == null) {
			if (other.sdks != null)
				return false;
		} else if (!sdks.equals(other.sdks))
			return false;
		return true;
	}

}
