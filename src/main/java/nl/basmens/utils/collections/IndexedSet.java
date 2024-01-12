package nl.basmens.utils.collections;

import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class IndexedSet<E extends IndexedSet.IndexedSetElement> extends AbstractSet<E> {
  private ArrayList<E> elements = new ArrayList<>();

  private int getIndexOfElement(E elem) {
    int index = elem.getSetIndex();
    if (index < elements.size() && elements.get(index) == elem) {
      return index;
    }
    return -1;
  }

  public E getAny() {
    if (elements.isEmpty()) {
      return null;
    }
    return elements.get(0);
  }

  @Override
  public boolean add(E e) {
    if (contains(e)) {
      return false;
    }
    e.setSetIndex(elements.size());
    elements.add(e);
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
    elements.clear();
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
      int index = getIndexOfElement(elem);
      if (index != -1) {
        E lastElem = elements.get(elements.size() - 1);
        lastElem.setSetIndex(index);
        elements.set(index, lastElem);
        elements.remove(elements.size() - 1);
        return true;
      }
      return false;
    } catch (ClassCastException e) {
      return false;
    }
  }

  @Override
  public boolean isEmpty() {
    return elements.isEmpty();
  }

  @Override
  public Iterator<E> iterator() {
    return elements.iterator();
  }

  @Override
  public int size() {
    return elements.size();
  }

  @Override
  public Object[] toArray() {
    return elements.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return elements.toArray(a);
  }

  @Override
  public String toString() {
    return elements.toString();
  }

  @Override
  public boolean removeIf(Predicate<? super E> filter) {
    return elements.removeIf(filter);
  }

  @Override
  public Stream<E> stream() {
    return elements.stream();
  }

  @Override
  public <T> T[] toArray(IntFunction<T[]> generator) {
    return elements.toArray(generator);
  }

  @Override
  public void forEach(Consumer<? super E> action) {
    elements.forEach(action);
  }

  public abstract interface IndexedSetElement {
    int getSetIndex();

    void setSetIndex(int setIndex);
  }
}
