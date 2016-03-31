/*
 * The MIT License
 *
 *   Copyright (c) 2016, Mahmoud Ben Hassine (mahmoud.benhassine@icloud.com)
 *
 *   Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 *   The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *   IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *   FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *   AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *   LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *   OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *   THE SOFTWARE.
 */

package io.github.benas.randombeans.util;

import static io.github.benas.randombeans.util.ReflectionUtils.isAbstract;
import static io.github.benas.randombeans.util.ReflectionUtils.isPublic;
import static java.util.stream.Collectors.toList;
import static org.reflections.util.ClasspathHelper.forJavaClassPath;
import static org.reflections.util.ConfigurationBuilder.build;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.reflections.Reflections;

/**
 * Facade for {@link org.reflections.Reflections}. It is a separate class from {@link ReflectionUtils},
 * so that the classpath scanning - which can take a few seconds - is only done when necessary.
 */
abstract class ReflectionsFacade {

    private static final ConcurrentHashMap<Class<?>, List<Class<?>>> typeToConcreteSubTypes;

    private static final Reflections reflections;

    static {
        typeToConcreteSubTypes = new ConcurrentHashMap<>();
        reflections = new Reflections(build().setUrls(forJavaClassPath()));
    }

    /**
     * Searches the classpath for all public concrete subtypes of the given interface or abstract class.
     *
     * @param type to search concrete subtypes of
     * @return a list of all concrete subtypes found
     */
    public static <T> List<Class<?>> getPublicConcreteSubTypesOf(final Class<T> type) {
        List<Class<?>> concreteSubTypes = typeToConcreteSubTypes.get(type);
        if (concreteSubTypes == null) {
            concreteSubTypes = searchForPublicConcreteSubTypesOf(type);
            typeToConcreteSubTypes.putIfAbsent(type, Collections.unmodifiableList(concreteSubTypes));
        }
        return concreteSubTypes;
    }

    private static <T> List<Class<?>> searchForPublicConcreteSubTypesOf(final Class<T> type) {
        Set<Class<? extends T>> subTypes = reflections.getSubTypesOf(type);
        return subTypes.stream().filter(currentSubType -> isPublic(currentSubType) && !(isAbstract(currentSubType))).collect(toList());
    }
}