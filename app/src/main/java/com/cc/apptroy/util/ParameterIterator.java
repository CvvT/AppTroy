package com.cc.apptroy.util;

import android.util.Log;

import com.google.common.collect.ImmutableSet;

import org.cc.dexlib2.base.BaseMethodParameter;
import org.cc.dexlib2.iface.Annotation;
import org.cc.dexlib2.iface.MethodParameter;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by CwT on 15/12/3.
 */
public class ParameterIterator implements Iterator<MethodParameter> {
    private final Class<?>[] parameterTypes;
    private final Iterator<? extends Set<? extends Annotation>> parameterAnnotations;
    private final Iterator<String> parameterNames;
    private int index = 0;

    public ParameterIterator(@Nonnull Class<?>[] parameterTypes,
                             @Nonnull List<? extends Set<? extends Annotation>> parameterAnnotations,
                             @Nonnull Iterator<String> parameterNames) {
        this.parameterTypes = parameterTypes;
        this.parameterAnnotations = parameterAnnotations.iterator();
        this.parameterNames = parameterNames;
    }

    @Override public boolean hasNext() {
        return index < parameterTypes.length;
    }

    @Override public MethodParameter next() {
        //be care of the index
        @Nonnull final String type = Utility.dottoslash(parameterTypes[index++].getName());
        @Nonnull final Set<? extends Annotation> annotations;
        @Nullable final String name;

        if (parameterAnnotations.hasNext()) {
            annotations = parameterAnnotations.next();
        } else {
            annotations = ImmutableSet.of();
        }

        if (parameterNames.hasNext()) {
            name = parameterNames.next();
        } else {
            name = null;
        }

        return new BaseMethodParameter() {
            @Nonnull @Override public Set<? extends Annotation> getAnnotations() { return annotations; }
            @Nullable @Override public String getName() { return name; }
            @Nonnull @Override public String getType() { return type; }
        };
    }

    @Override public void remove() {
        throw new UnsupportedOperationException();
    }
}

