package diff;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import impact.Resources;

import models.Changeset;
import models.Range;

public class UnifiedDiffParser
{
	private String unifiedDiff;

	private Changeset currentChangeset;

	public UnifiedDiffParser() {

	}

	public UnifiedDiffParser(String unifiedDiff) {
		this.unifiedDiff = unifiedDiff;
	}

	public List<Changeset> parse() {
		if(unifiedDiff == null)
			return null;
		return parseUnifiedDiff();
	}

	public List<Changeset> parse(String unifiedDiff) {
		this.unifiedDiff = unifiedDiff;
		return parseUnifiedDiff();
	}

	private List<Changeset> parseUnifiedDiff() {
		List<Changeset> changes = new ArrayList<Changeset>();
		this.currentChangeset = null;

		String[] lines = unifiedDiff.split(System.getProperty("line.separator"));
		for(int i = 0; i < lines.length; i++) {
			parseDiffLine(lines[i], changes);
		}
		if(currentChangeset != null)
			changes.add(currentChangeset);

		return changes;
	}

	private void parseDiffLine(String line, List<Changeset> changes) {
		if(line.matches(Resources.diffOldFile)) {
			if(currentChangeset != null)
				changes.add(currentChangeset);

			currentChangeset = new Changeset();
			String oldFile = parseDiffOldFile(line);

			if(oldFile.startsWith("a/"))
				oldFile = oldFile.replaceFirst("a/", "");

			currentChangeset.setOldFile(oldFile);
		}
		else if(line.matches(Resources.diffNewFile)) {
			if(currentChangeset == null)
				currentChangeset = new Changeset();

			String newFile = parseDiffNewFile(line);
			if(newFile.startsWith("b/"))
				newFile = newFile.replaceFirst("b/", "");

			currentChangeset.setNewFile(newFile);
		}
		else if(line.matches(Resources.diffHunkRange)) {
			Range range = parseDiffHunkRange(line);
			if(range != null)
				currentChangeset.addRange(range);
		}
	}

	private String parseDiffOldFile(String line) {
		String[] split = line.split(" ");
		if(split.length == 2)
			return split[1];
		return "";
	}

	private String parseDiffNewFile(String line) {
		String[] split = line.split(" ");
		if(split.length == 2)
			return split[1];
		return "";
	}

	private Range parseDiffHunkRange(String line) {
		Range range = new Range();

		Pattern pattern = Pattern.compile(Resources.diffNewRange);
		Matcher matcher = pattern.matcher(line);

		if(matcher.find()) {
			String[] ranges = matcher.group().split(",");
			// Strip the + symbol
			ranges[0] = ranges[0].substring(1);
			range.setStart(Integer.parseInt(ranges[0]));
			if(ranges.length > 1)
				range.setEnd(Integer.parseInt(ranges[0]) + Integer.parseInt(ranges[1]));
			else
				range.setEnd(Integer.parseInt(ranges[0]));

			return range;
		}
		else
			return null;
	}
}