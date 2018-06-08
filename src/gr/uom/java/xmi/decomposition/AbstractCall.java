package gr.uom.java.xmi.decomposition;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gr.uom.java.xmi.LocationInfo;
import gr.uom.java.xmi.LocationInfoProvider;
import gr.uom.java.xmi.decomposition.replacement.Replacement;
import gr.uom.java.xmi.decomposition.replacement.Replacement.ReplacementType;

public abstract class AbstractCall implements LocationInfoProvider {
	protected int typeArguments;
	protected String expression;
	protected List<String> arguments;
	protected LocationInfo locationInfo;
	protected StatementCoverageType coverage = StatementCoverageType.NONE;

	public String getExpression() {
		return expression;
	}

	public List<String> getArguments() {
		return arguments;
	}

	public LocationInfo getLocationInfo() {
		return locationInfo;
	}

	public StatementCoverageType getCoverage() {
		return coverage;
	}

	public abstract boolean identicalName(AbstractCall call);
	public abstract double normalizedNameDistance(AbstractCall call);

	public boolean identicalExpression(AbstractCall call, Set<Replacement> replacements) {
		return identicalExpression(call) ||
		identicalExpressionAfterTypeReplacements(call, replacements);
	}

	public boolean identicalExpression(AbstractCall call) {
		return (getExpression() != null && call.getExpression() != null &&
				getExpression().equals(call.getExpression())) ||
				(getExpression() == null && call.getExpression() == null);
	}

	public boolean identicalExpressionAfterTypeReplacements(AbstractCall call, Set<Replacement> replacements) {
		if(getExpression() != null && call.getExpression() != null) {
			String expression1 = getExpression();
			String expression2 = call.getExpression();
			String expression1AfterReplacements = new String(expression1);
			for(Replacement replacement : replacements) {
				if(replacement.getType().equals(ReplacementType.TYPE)) {
					expression1AfterReplacements = ReplacementUtil.performReplacement(expression1AfterReplacements, replacement.getBefore(), replacement.getAfter());
				}
			}
			if(expression1AfterReplacements.equals(expression2)) {
				return true;
			}
		}
		return false;
	}

	public boolean equalArguments(AbstractCall call) {
		return getArguments().equals(call.getArguments());
	}

	public boolean identicalArguments(AbstractCall call, Set<String> set1, Set<String> set2) {
		List<String> arguments1 = getArguments();
		List<String> arguments2 = call.getArguments();
		if(arguments1.size() != arguments2.size())
			return false;
		for(int i=0; i<arguments1.size(); i++) {
			String argument1 = arguments1.get(i);
			String argument2 = arguments2.get(i);
			if(!argument1.equals(argument2) && !set1.contains(argument2) && !set2.contains(argument1))
				return false;
		}
		return true;
	}

	public boolean identicalOrReplacedArguments(AbstractCall call, Set<Replacement> replacements) {
		List<String> arguments1 = getArguments();
		List<String> arguments2 = call.getArguments();
		if(arguments1.size() != arguments2.size())
			return false;
		for(int i=0; i<arguments1.size(); i++) {
			String argument1 = arguments1.get(i);
			String argument2 = arguments2.get(i);
			boolean argumentReplacement = false;
			for(Replacement replacement : replacements) {
				if(replacement.getBefore().equals(argument1) &&	replacement.getAfter().equals(argument2)) {
					argumentReplacement = true;
					break;
				}
			}
			if(!argument1.equals(argument2) && !argumentReplacement)
				return false;
		}
		return true;
	}

	public boolean allArgumentsReplaced(AbstractCall call, Set<Replacement> replacements, Map<String, String> parameterToArgumentMap) {
		int replacedArguments = 0;
		List<String> arguments1 = getArguments();
		List<String> arguments2 = call.getArguments();
		if(arguments1.size() == arguments2.size()) {
			for(int i=0; i<arguments1.size(); i++) {
				String argument1 = arguments1.get(i);
				String argument2 = arguments2.get(i);
				for(Replacement replacement : replacements) {
					if( (replacement.getBefore().equals(argument1) || replacement.getBefore().equals(parameterToArgumentMap.get(argument1))) &&
							(replacement.getAfter().equals(argument2) || replacement.getAfter().equals(parameterToArgumentMap.get(argument2))) ) {
						replacedArguments++;
						break;
					}
				}
			}
		}
		return replacedArguments > 0 && replacedArguments == arguments1.size();
	}

	public boolean renamedWithIdenticalExpressionAndArguments(AbstractCall call, Set<Replacement> replacements) {
		return getExpression() != null && call.getExpression() != null &&
				identicalExpression(call, replacements) &&
				!identicalName(call) &&
				equalArguments(call);
	}

	public boolean renamedWithIdenticalArgumentsAndNoExpression(AbstractCall call, double distance) {
		return getExpression() == null && call.getExpression() == null &&
				!identicalName(call) &&
				normalizedNameDistance(call) <= distance &&
				equalArguments(call);
	}

	public boolean renamedWithIdenticalExpressionAndDifferentNumberOfArguments(AbstractCall call, Set<String> set1, Set<String> set2, Set<Replacement> replacements, double distance) {
		return getExpression() != null && call.getExpression() != null &&
				identicalExpression(call, replacements) &&
				normalizedNameDistance(call) <= distance &&
				!identicalArguments(call, set1, set2) &&
				getArguments().size() != call.getArguments().size();
	}

	public boolean onlyArgumentsChanged(AbstractCall call, Set<String> set1, Set<String> set2, Set<Replacement> replacements) {
		return identicalExpression(call, replacements) &&
				identicalName(call) &&
				!identicalArguments(call, set1, set2) &&
				getArguments().size() != call.getArguments().size();
	}

	public boolean identical(AbstractCall call,
			Set<String> set1, Set<String> set2, Set<Replacement> replacements) {
		return identicalExpression(call, replacements) &&
				identicalName(call) &&
				identicalArguments(call, set1, set2);
	}

	public Set<String> argumentIntersection(AbstractCall call) {
		Set<String> argumentIntersection = new LinkedHashSet<String>(getArguments());
		argumentIntersection.retainAll(call.getArguments());
		return argumentIntersection;
	}

	public enum StatementCoverageType {
		NONE, ONLY_CALL, RETURN_CALL, THROW_CALL, CAST_CALL, VARIABLE_DECLARATION_INITIALIZER_CALL;
	}
}