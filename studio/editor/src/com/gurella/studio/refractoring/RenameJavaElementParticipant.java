package com.gurella.studio.refractoring;

import static com.gurella.studio.refractoring.RefractoringUtils.qualifiedNamesHandledByProcessor;

import java.util.regex.Pattern;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;

import com.gurella.engine.utils.Values;

public class RenameJavaElementParticipant extends RenameParticipant {
	private IJavaElement element;

	@Override
	protected boolean initialize(Object element) {
		this.element = (IJavaElement) element;
		return true;
	}

	@Override
	public String getName() {
		return "Gurella java element rename participant";
	}

	@Override
	public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context)
			throws OperationCanceledException {
		return new RefactoringStatus();
	}

	@Override
	public Change createChange(IProgressMonitor monitor) throws CoreException, OperationCanceledException {
		IResource[] rootResources = RefractoringUtils.getSearchScopeRootResources(element.getResource().getProject());
		if (Values.isEmpty(rootResources)) {
			return null;
		}

		String oldName = element instanceof IType ? ((IType) element).getFullyQualifiedName()
				: element.getElementName();
		int index = oldName.lastIndexOf(element.getElementName());
		String newName = oldName.substring(0, index).concat(getArguments().getNewName());

		boolean txtFilesHandled = qualifiedNamesHandledByProcessor(getProcessor());
		String regex = "(?<=[[:|\\s|\\r|\\n]{1}[\\s|\\r|\\n]{0,100}]|^)" + Pattern.quote(oldName)
				+ "(?=\"|\\.|\\s|\\r|\\n|$)";
		return element instanceof IType
				? RefractoringUtils.createChange(txtFilesHandled, monitor, rootResources, regex, newName)
				: RefractoringUtils.createPackageMoveChange(txtFilesHandled, monitor, rootResources, regex, newName,
						element);
	}
}
