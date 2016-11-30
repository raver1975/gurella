package com.gurella.studio.refractoring;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IType;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.eclipse.search.core.text.TextSearchEngine;
import org.eclipse.search.core.text.TextSearchRequestor;
import org.eclipse.search.ui.text.FileTextSearchScope;

import com.gurella.engine.asset.Assets;
import com.gurella.studio.editor.utils.JdtUtils;

public class RenameAssetsParticipant extends RenameParticipant {
	private IFile file;

	@Override
	protected boolean initialize(Object element) {
		file = (IFile) element;
		return true;
	}

	@Override
	public String getName() {
		return "Gurella asset rename participant";
	}

	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context)
			throws OperationCanceledException {
		return new RefactoringStatus();
	}

	@Override
	public Change createChange(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
		boolean isJavaFile = "java".equals(file.getProjectRelativePath().getFileExtension());
		if (!isJavaFile && Assets.getAssetType(file.getName()) == null) {
			return null;
		}

		final Map<IFile, TextFileChange> changes = new HashMap<>();
		IProject project = file.getProject();
		IPath assetsFolderPath = project.getProjectRelativePath().append("assets");
		IResource[] roots = { project };
		String[] fileNamePatterns = { "*.pref", "*.gscn", "*.gmat", "*.glslt", "*.grt", "*.giam" };
		IPath oldResourcePath = file.getProjectRelativePath().makeRelativeTo(assetsFolderPath);
		String newName = getArguments().getNewName();
		IPath newResourcePath = oldResourcePath.removeLastSegments(1).append(newName);

		if (isJavaFile) {
			String oldCuName = file.getFullPath().removeFileExtension().lastSegment();
			String newCuName = newResourcePath.removeFileExtension().lastSegment();
			ICompilationUnit compilationUnit = JdtUtils.getCompilationUnit(file);
			
			for (IType type : compilationUnit.getAllTypes()) {
				String qualifiedName = type.getFullyQualifiedName('.');

			}
		} else {
			FileTextSearchScope scope = FileTextSearchScope.newSearchScope(roots, fileNamePatterns, false);
			TextSearchRequestor requestor = new RenameAssetRequestor(changes, newResourcePath.toString());
			Pattern pattern = Pattern.compile(oldResourcePath.toString());
			TextSearchEngine.create().search(scope, requestor, pattern, monitor);
		}

		if (changes.isEmpty()) {
			return null;
		}

		CompositeChange result = new CompositeChange("Gurella asset references update");
		changes.values().forEach(result::add);
		return result;
	}
}
