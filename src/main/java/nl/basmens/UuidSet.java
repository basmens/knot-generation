package nl.basmens;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class UuidSet<E extends UuidSet.UuidSetElement> extends AbstractSet<E> {
  private ArrayList<E> list = new ArrayList<>();
  private int uuidCounter;
  private int removedElementsCount;

  private int getIndexOfUuid(int uuid, int lowerBound, int upperBound) {
    int guess = (lowerBound + upperBound) / 2;
    int guessedUuid = list.get(guess).getUuid();
    if (guessedUuid == uuid) {
      return guess;
    } else if (lowerBound == upperBound - 1) {
      return -1;
    } else if (guessedUuid > uuid) {
      return getIndexOfUuid(uuid, lowerBound, guessedUuid);
    } else {
      return getIndexOfUuid(uuid, guessedUuid, upperBound);
    }
  }

  private int getIndexOfElement(E elem) {
    if (list.isEmpty()) {
      return -1;
    }
    int uuid = elem.getUuid();
    int index = (uuid == 0) ? 0
        : getIndexOfUuid(uuid, Math.max(uuid - removedElementsCount, 0), Math.min(uuid + 1, list.size()));
    if (list.get(index) == elem) {
      return index;
    }
    return -1;
  }

  public E getAny() {
    if (list.isEmpty()) {
      return null;
    }
    return list.get(0);
  }

  @Override
  public boolean add(E e) {
    if (!list.isEmpty() && contains(e)) {
      return false;
    }
    e.setUuid(uuidCounter);
    uuidCounter++;
    list.add(e);
    return true;
  }

  @Override
  public boolean addAll(Collection<? extends E> c) {
    boolean isModified = false;
    for (E e : c) {
      isModified |= add(e);
    }
    return isModified;
  }

  @Override
  public void clear() {
    list.clear();
    uuidCounter = 0;
    removedElementsCount = 0;
  }

  @Override
  public boolean contains(Object o) {
    try {
      E elem = (E) o;
      int index = getIndexOfElement(elem);
      return index != -1;
    } catch (ClassCastException e) {
      return false;
    }
  }

  @Override
  public boolean remove(Object o) {
    try {
      E elem = (E) o;
      int uuid = elem.getUuid();
      int index = getIndexOfElement(elem);
      if (index != -1) {
        E lastElem = list.get(list.size() - 1);
        lastElem.setUuid(uuid);
        elem.setUuid(0);
        list.set(index, lastElem);
        list.remove(list.size() - 1);
        removedElementsCount++;
        return true;
      }
    } catch (ClassCastException e) {
      return false;
    }
    return false;
  }

  @Override
  public boolean isEmpty() {
    return list.isEmpty();
  }

  @Override
  public Iterator<E> iterator() {
    return list.iterator();
  }

  @Override
  public int size() {
    return list.size();
  }

  @Override
  public Object[] toArray() {
    return list.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return list.toArray(a);
  }

  @Override
  public String toString() {
    return list.toString();
  }

  @Override
  public boolean removeIf(Predicate<? super E> filter) {
    return list.removeIf(filter);
  }

  @Override
  public Stream<E> stream() {
    return list.stream();
  }

  @Override
  public <T> T[] toArray(IntFunction<T[]> generator) {
    return list.toArray(generator);
  }

  @Override
  public void forEach(Consumer<? super E> action) {
    list.forEach(action);
  }

  public abstract interface UuidSetElement {
    int getUuid();

    void setUuid(int uuid);
  }
}
