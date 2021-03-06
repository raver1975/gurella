package com.gurella.studio.refractoring;

import static com.gurella.studio.common.AssetsFolderLocator.assetsFolderName;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.CopyParticipant;
import org.eclipse.search.core.text.TextSearchEngine;
import org.eclipse.search.core.text.TextSearchRequestor;
import org.eclipse.search.ui.text.FileTextSearchScope;

import com.gurella.engine.asset.descriptor.AssetDescriptor;
import com.gurella.engine.asset.descriptor.AssetDescriptors;

public class CopyAssetsParticipant extends CopyParticipant {
	private IFile file;

	@Override
	protected boolean initialize(Object element) {
		file = (IFile) element;
		return true;
	}

	@Override
	public String getName() {
		return "Gurella asset copy participant";
	}

	@Override
	public RefactoringStatus checkConditions(IProgressMonitor monitor, CheckConditionsContext context)
			throws OperationCanceledException {
		return new RefactoringStatus();
	}

	@Override
	public Change createChange(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
		if (!assetsFolderName.equals(file.getProjectRelativePath().segment(0))) {
			return null;
		}

		AssetDescriptor<?> descriptor = AssetDescriptors.getAssetDescriptor(file.getName());
		if (descriptor == null || !descriptor.hasReferences) {
			return null;
		}

		final IContainer destination = (IContainer) getArguments().getDestination();
		IFile copy = destination.getFile(Path.fromPortableString(file.getName()));
		IResource[] roots = new IResource[] { file };

		FileTextSearchScope scope = FileTextSearchScope.newSearchScope(roots, new String[] { "*" }, false);
		final Map<IFile, TextFileChange> changes = new HashMap<>();
		TextSearchRequestor requestor = new GenerateUuidRequestor(changes, copy);
		String regex = "(?<=uuid[\"]{0,1}[\\s|\\r|\\n]{0,100}:[\\s|\\r|\\n]{1,100})([0-9a-fA-F]{32})(?=\\s|\\r|\\n|$)";
		TextSearchEngine.create().search(scope, requestor, Pattern.compile(regex), monitor);

		if (changes.isEmpty()) {
			return null;
		}

		CompositeChange result = new CompositeChange("Gurella asset references update");
		changes.values().forEach(result::add);
		return result;
	}
}
