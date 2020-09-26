package org.cliu;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

// Builds an iterator of the Cartesian product using a long[][] array
// Follows an implementation outlined in https://stackoverflow.com/a/9449311
public class CartesianProductOfLongsIterator {
    public static class Product {
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
            return "CartesianProductOfLongsIterator.product(" + Arrays.toString(_axes) + ")";
        }

        public static class ProductIterator {
            private final long[][] _axes;
            // keeps track of the index each iterator is on.
            private final int[] _indices;
            private final long[] _result; // a copy of the last result

            private int _nextIndex = NEW;
            private static final int NEW = -2;
            private static final int DONE = -1;

            /**
             * Caution: the given array of axes is contained by reference, not cloned.
             */
            ProductIterator(long[][] axes) {
                _axes = axes;
                _indices = new int[_axes.length];
                _result = new long[_axes.length];
            }

            private void close() {
                _nextIndex = DONE;
            }

            public boolean hasNext() {
                if (_nextIndex == NEW) { // This is the first call to hasNext().
                    _nextIndex = 0; // start here
                    for (int i=0;i<_axes.length;i++) {
                        if (_axes[i].length == 0) {
                            close(); // no combinations
                            break;
                        }
                    }
                } else if (_nextIndex >= _axes.length) {
                    // This is the first call to hasNext() after next() returned a result.
                    // Determine the _nextIndex to be used by next():
                    for (_nextIndex = _axes.length - 1; _nextIndex >= 0; --_nextIndex) {
                        var axis = _axes[_nextIndex];
                        if (_indices[_nextIndex] < axis.length -1) {
                            _indices[_nextIndex] += 1;
                            break; // start here
                        }
                        if (_nextIndex == 0) { // All combinations have been generated.
                            close();
                            break;
                        }
                        // Repeat this axis, with the next value from the previous axis.
                        _indices[_nextIndex] = 0;
                        if (_axes[_nextIndex].length == 0) { // Oops; this axis can't be repeated.
                            close(); // no more combinations
                            break;
                        }
                    }
                }
                return _nextIndex >= 0;
            }

            public long[] nextLongs() {
                // UNSAFE HERE
//                if (!hasNext())
//                    throw new NoSuchElementException("!hasNext");
                for (; _nextIndex < _axes.length; ++_nextIndex) {
                    _result[_nextIndex] = _axes[_nextIndex][_indices[_nextIndex]];
                }
                return _result;
            }

            @Override
            public String toString() {
                return "CartesianProductOfLongsIterator.product(" + Arrays.toString(_axes) + ").iterator()";
            }
        }
    }
}
