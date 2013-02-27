package com.cordys.jenkinsci.summareport.helpers;

import hudson.model.Job;
import org.apache.commons.collections.iterators.FilterIterator;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class CollectionHelper {
    public static java.lang.Iterable<Job> getUniqueJobsByName(java.lang.Iterable<Job> jobs) {
        return new Iterable<Job>(new FilterIterator(jobs.iterator(), new Predicate<Job>() {
            private final Set set = new HashSet();

            public boolean eval(Job object) {
                return set.add(object.getName());
            }
        }));
    }

    public static class Iterable<T> implements java.lang.Iterable<T> {
        private final Iterator<T> inner;

        public Iterable(Iterator<T> inner) {
            this.inner = inner;
        }

        public Iterator<T> iterator() {
            return inner;
        }
    }

    public static abstract class Predicate<T> implements org.apache.commons.collections.Predicate {

        /**
         * Use the specified parameter to perform a test that returns true or false.
         *
         * @param object the object to evaluate, should not be changed
         * @return true or false
         * @throws ClassCastException       (runtime) if the input is the wrong class
         * @throws IllegalArgumentException (runtime) if the input is invalid
         * @throws org.apache.commons.collections.FunctorException
         *                                  (runtime) if the predicate encounters a problem
         */
        @Deprecated
        public boolean evaluate(Object object) {
            return eval((T) object);
        }

        /**
         * Use the specified parameter to perform a test that returns true or false.
         *
         * @param object the object to evaluate, should not be changed
         * @return true or false
         * @throws ClassCastException       (runtime) if the input is the wrong class
         * @throws IllegalArgumentException (runtime) if the input is invalid
         * @throws org.apache.commons.collections.FunctorException
         *                                  (runtime) if the predicate encounters a problem
         */
        public abstract boolean eval(T object);

    }

    public static abstract class Closure<T> implements org.apache.commons.collections.Closure {
        /**
         * Performs an action on the specified input object.
         *
         * @param input the input to execute on
         * @throws ClassCastException       (runtime) if the input is the wrong class
         * @throws IllegalArgumentException (runtime) if the input is invalid
         * @throws org.apache.commons.collections.FunctorException
         *                                  (runtime) if any other error occurs
         */
        @Deprecated
        public void execute(Object input) {
            exec((T) input);
        }

        protected abstract void exec(T input);
    }
}
