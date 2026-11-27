package designbook.solutions8.ex06;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

import designbook.solutions8.Show;

/** Exercise 8.6: Observer, push data-flow, một interface observer. */
public class Exercise06 {
	public static void main(String[] args) {
		Program program = new Program();
		program.addObserver(new DummyObserver());

		Show show = new Show() {
			public String description() { return "Test show"; }
			public int runningTime() { return 90; }
			public Show copy() { return this; }
		};

		program.add(show, Program.Day.MONDAY);
		program.remove(Program.Day.MONDAY);
		program.add(show, Program.Day.FRIDAY);
		program.clear();
	}
}

/** Observer duy nhất, dữ liệu được PUSH qua tham số. */
interface ProgramObserver {
	void showAdded(Program.Day pDay, Show pShow);
	void showRemoved(Program.Day pDay, Show pShow);
	void programCleared();
}

class Program {
	public enum Day {
		MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
	}

	private static final Show NULL_SHOW = createNullShow();

	private static Show createNullShow() {
		return new Show() {
			public String description() { return ""; }
			public int runningTime() { return 0; }
			public Show copy() { return createNullShow(); }
			public int hashCode() { return 0; }
			public boolean equals(Object pObject) {
				return pObject != null && pObject.getClass() == this.getClass();
			}
		};
	}

	private final EnumMap<Day, Show> aShows = new EnumMap<>(Day.class);
	private final List<ProgramObserver> aObservers = new ArrayList<>();

	public Program() {
		reset();
	}

	private void reset() {
		for (Day day : Day.values()) {
			aShows.put(day, NULL_SHOW);
		}
	}

	public void addObserver(ProgramObserver pObserver) {
		assert pObserver != null;
		aObservers.add(pObserver);
	}

	public void clear() {
		reset();
		for (ProgramObserver observer : aObservers) {
			observer.programCleared();
		}
	}

	public void add(Show pShow, Day pDay) {
		assert pShow != null && pDay != null;
		aShows.put(pDay, pShow);
		for (ProgramObserver observer : aObservers) {
			observer.showAdded(pDay, pShow.copy());
		}
	}

	public void remove(Day pDay) {
		assert pDay != null;
		Show removed = aShows.get(pDay);
		aShows.put(pDay, NULL_SHOW);
		for (ProgramObserver observer : aObservers) {
			observer.showRemoved(pDay, removed.copy());
		}
	}

	public Show get(Day pDay) {
		assert pDay != null;
		return aShows.get(pDay);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (Day day : aShows.keySet()) {
			result.append(String.format("%9s", day.name()))
				  .append(": ").append(aShows.get(day).description()).append("\n");
		}
		return result.toString();
	}
}

/** Observer giả để kiểm thử. */
class DummyObserver implements ProgramObserver {
	public void showAdded(Program.Day pDay, Show pShow) {
		System.out.println("[Dummy] ADDED   " + pDay + ": " + pShow.description());
	}
	public void showRemoved(Program.Day pDay, Show pShow) {
		System.out.println("[Dummy] REMOVED " + pDay + ": " + pShow.description());
	}
	public void programCleared() {
		System.out.println("[Dummy] CLEARED");
	}
}
