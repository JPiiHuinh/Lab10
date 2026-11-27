package designbook.solutions8.ex07;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;

import designbook.solutions8.Show;

/** Exercise 8.7: Observer, pull data-flow, hai interface observer, ISP. */
public class Exercise07 {
	public static void main(String[] args) {
		ObservableProgram program = new ObservableProgram();
		program.addObserver(new AdditionLogger());
		program.addObserver(new RemovalLogger());
		FullLogger full = new FullLogger();
		program.addObserver((ProgramAdditionObserver) full);
		program.addObserver((ProgramRemovalObserver) full);

		Show show = new Show() {
			public String description() { return "Test show"; }
			public int runningTime() { return 90; }
			public Show copy() { return this; }
		};

		program.add(show, ObservableProgram.Day.MONDAY);
		program.remove(ObservableProgram.Day.MONDAY);
		program.clear();
	}
}

/** Observer chỉ quan tâm sự kiện thêm show. */
interface ProgramAdditionObserver {
	void showAdded(Iterable<Show> pShows);
}

/** Observer quan tâm sự kiện xóa một show hoặc xóa toàn bộ chương trình. */
interface ProgramRemovalObserver {
	void showRemoved(Iterable<Show> pShows);
	void programCleared();
}

class ObservableProgram implements Iterable<Show> {
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
	private final List<ProgramAdditionObserver> aAdditionObservers = new ArrayList<>();
	private final List<ProgramRemovalObserver> aRemovalObservers = new ArrayList<>();

	public ObservableProgram() {
		reset();
	}

	private void reset() {
		for (Day day : Day.values()) {
			aShows.put(day, NULL_SHOW);
		}
	}

	public void addObserver(ProgramAdditionObserver pObserver) {
		assert pObserver != null;
		aAdditionObservers.add(pObserver);
	}

	public void addObserver(ProgramRemovalObserver pObserver) {
		assert pObserver != null;
		aRemovalObservers.add(pObserver);
	}

	public void add(Show pShow, Day pDay) {
		assert pShow != null && pDay != null;
		aShows.put(pDay, pShow);
		for (ProgramAdditionObserver observer : aAdditionObservers) {
			observer.showAdded(this);
		}
	}

	public void remove(Day pDay) {
		assert pDay != null;
		aShows.put(pDay, NULL_SHOW);
		for (ProgramRemovalObserver observer : aRemovalObservers) {
			observer.showRemoved(this);
		}
	}

	public void clear() {
		reset();
		for (ProgramRemovalObserver observer : aRemovalObservers) {
			observer.programCleared();
		}
	}

	public Show get(Day pDay) {
		assert pDay != null;
		return aShows.get(pDay);
	}

	/** Duyệt 7 ngày theo thứ tự cố định; iterator không cho remove(). */
	@Override
	public Iterator<Show> iterator() {
		return Collections.unmodifiableCollection(aShows.values()).iterator();
	}
}

/** Observer cụ thể 1: chỉ nghe sự kiện thêm. */
class AdditionLogger implements ProgramAdditionObserver {
	public void showAdded(Iterable<Show> pShows) {
		System.out.println("[Addition] Cac show hien co: " + describe(pShows));
	}

	static String describe(Iterable<Show> pShows) {
		StringBuilder sb = new StringBuilder();
		int total = 0;
		for (Show show : pShows) {
			if (show.runningTime() > 0) {
				sb.append(show.description()).append("; ");
				total += show.runningTime();
			}
		}
		return sb + "(tong " + total + " phut)";
	}
}

/** Observer cụ thể 2: chỉ nghe sự kiện xóa. */
class RemovalLogger implements ProgramRemovalObserver {
	public void showRemoved(Iterable<Show> pShows) {
		System.out.println("[Removal] Sau khi xoa: " + AdditionLogger.describe(pShows));
	}
	public void programCleared() {
		System.out.println("[Removal] Chuong trinh da duoc xoa sach");
	}
}

/** Observer cụ thể 3: nghe cả hai loại sự kiện. */
class FullLogger implements ProgramAdditionObserver, ProgramRemovalObserver {
	public void showAdded(Iterable<Show> pShows) {
		System.out.println("[Full] added   -> " + AdditionLogger.describe(pShows));
	}
	public void showRemoved(Iterable<Show> pShows) {
		System.out.println("[Full] removed -> " + AdditionLogger.describe(pShows));
	}
	public void programCleared() {
		System.out.println("[Full] cleared");
	}
}
