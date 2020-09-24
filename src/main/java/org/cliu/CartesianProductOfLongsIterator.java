package org.cliu;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class CartesianProductOfLongsIterator {
    public static class Product  {
        private final long[][] _axes;

        public Product(long[][] axes) {
            _axes = axes;
        }

        public ProductIterator iterator() {
            if (_axes.length <= 0) // an edge case
                return new ProductIterator(new long[0][0]) {
                    @Override
                    public boolean hasNext() {
                        return false;
                    }
                };
            return new CartesianProductOfLongsIterator.Product.ProductIterator(_axes);
        }

        @Override
        public String toString() {
            return "Cartesian.product(" + Arrays.toString(_axes) + ")";
        }

        public static class ProductIterator {
            private final long[][] _axes;
            private final long[] _indices;
            private final Iterator<Long>[] _iterators; // one per axis
            private final long[] _result; // a copy of the last result
            /**
             * The minimum index such that this.next() will return an array that contains
             * _iterators[index].next(). There are some special sentinel values: NEW means this
             * is a freshly constructed iterator, DONE means all combinations have been
             * exhausted (so this.hasNext() == false) and _iterators.length means the value is
             * unknown (to be determined by this.hasNext).
             */
            private int _nextIndex = NEW;
            private static final int NEW = -2;
            private static final int DONE = -1;

            /**
             * Caution: the given array of axes is contained by reference, not cloned.
             */
            ProductIterator(long[][] axes) {
                _axes = axes;
                _indices = new long[axes.length];
                _iterators = CartesianProductIterator.newArray(Iterator.class, _axes.length);
                for (int a = 0; a < _axes.length; ++a) {
                    _iterators[a] = Arrays.stream(axes[a]).iterator();
                }
                _result = new long[_iterators.length];
            }

            private void close() {
                _nextIndex = DONE;
            }

            public boolean hasNext() {
                if (_nextIndex == NEW) { // This is the first call to hasNext().
                    _nextIndex = 0; // start here
                    for (var iter : _iterators) {
                        if (!iter.hasNext()) {
                            close(); // no combinations
                            break;
                        }
                    }
                } else if (_nextIndex >= _iterators.length) {
                    // This is the first call to hasNext() after next() returned a result.
                    // Determine the _nextIndex to be used by next():
                    for (_nextIndex = _iterators.length - 1; _nextIndex >= 0; --_nextIndex) {
                        var iter = _iterators[_nextIndex];
                        if (iter.hasNext()) {
                            break; // start here
                        }
                        if (_nextIndex == 0) { // All combinations have been generated.
                            close();
                            break;
                        }
                        // Repeat this axis, with the next value from the previous axis.
                        iter = Arrays.stream(_axes[_nextIndex]).iterator();
                        _iterators[_nextIndex] = iter;
                        if (!iter.hasNext()) { // Oops; this axis can't be repeated.
                            close(); // no more combinations
                            break;
                        }
                    }
                }
                return _nextIndex >= 0;
            }

            public long[] nextLongs() {
//                if (!hasNext())
//                    throw new NoSuchElementException("!hasNext");
                for (; _nextIndex < _iterators.length; ++_nextIndex) {
                    _result[_nextIndex] = _iterators[_nextIndex].next();
                }
                return _result;
            }

            @Override
            public String toString() {
                return "Cartesian.product(" + Arrays.toString(_axes) + ").iterator()";
            }
        }
    }
}
