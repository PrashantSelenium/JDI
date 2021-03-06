package com.epam.jdi.uitests.web.selenium.elements.complex.table;

/*
 * Copyright 2004-2016 EPAM Systems
 *
 * This file is part of JDI project.
 *
 * JDI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JDI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JDI. If not, see <http://www.gnu.org/licenses/>.
 */

import com.epam.commons.LinqUtils;
import com.epam.commons.map.MapArray;
import com.epam.commons.pairs.Pair;
import com.epam.jdi.uitests.core.interfaces.complex.interfaces.Column;
import com.epam.jdi.uitests.core.interfaces.complex.interfaces.ICell;
import com.epam.jdi.uitests.core.interfaces.complex.interfaces.IEntityTable;
import com.epam.jdi.uitests.core.interfaces.complex.interfaces.TableFilter;
import com.epam.jdi.uitests.web.selenium.elements.WebCascadeInit;
import com.epam.jdi.uitests.web.selenium.elements.base.BaseElement;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Function;

import static com.epam.commons.LinqUtils.select;
import static com.epam.commons.LinqUtils.where;
import static com.epam.commons.PrintUtils.print;
import static com.epam.commons.ReflectionUtils.convertStringToType;
import static com.epam.jdi.uitests.core.interfaces.MapInterfaceToElement.getClassFromInterface;
import static com.epam.jdi.uitests.core.settings.JDISettings.exception;
import static java.lang.String.format;
import static org.apache.commons.lang3.reflect.FieldUtils.writeField;

/**
 * Created by Sergey_Mishanin on 11/18/16.
 */
public class EntityTable<E, R> extends Table implements IEntityTable<E,R> {
    private Class<E> entityClass;
    private Class<R> rowClass;

    public EntityTable(Class<E> entityClass){
        if (entityClass == null)
            throw new IllegalArgumentException("Entity type was not specified");

        this.entityClass = entityClass;
        hasColumnHeaders(select(entityClass.getFields(), Field::getName));
    }

    public EntityTable(Class<E> entityClass, Class<R> rowClass){
        this(entityClass);
        this.rowClass = rowClass;
    }

    private R newRow(){
        if (rowClass == null)
            throw exception("Row class was not specified");
        try {
            return rowClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw exception("Can't Instantiate row: " + rowClass.getName());
        }
    }

    private R castToRow(MapArray<String, ICell> row)
    {
        R newRow = newRow();
        row.pairs.forEach(pair ->
                setRowField(newRow, newRow.getClass().getFields(), pair.key, pair.value));
        return newRow;
    }

    private void setField(Object row, Field[] fields, String fieldName,
                          Function<Field, Object> valueFunc)
    {
        Field field = LinqUtils.first(fields, f -> f.getName().equalsIgnoreCase(fieldName));

        if (field == null) return;
        try {
            writeField(field, row, valueFunc.apply(field), true);
        } catch (IllegalAccessException e) {
            throw exception("Can't write field with name: " + fieldName);
        }
    }


    private void setRowField(R row, Field[] fields, String fieldName, ICell cell)
    {
        setField(row, fields, fieldName, field -> {
            Class clazz = field.getType();
            if (clazz == null) return null;
            BaseElement value;
            try {
                if (clazz.isInterface())
                    clazz = getClassFromInterface(clazz);
                value = (BaseElement) clazz.newInstance();
                new WebCascadeInit().initElements(value, avatar.getDriverName());
                value.init(avatar.getDriverName(), cell, cell.get().getAvatar());
            } catch (InstantiationException | IllegalAccessException e) {
                throw exception("Can't Instantiate row element: " + fieldName);
            }
            return value;
            });
    }

    public List<R> getRows() {
        return select(rows().get(), row -> castToRow(row.value));
    }

    public List<R> getRows(Function<R, Boolean> rule)
    {
        return where(getRows(), rule);
    }

    public R getRow(String value, Column column)
    {
        return castToRow(row(value, column));
    }

    public R getRow(int rowNum)
    {
        return castToRow(row(rowNum));
    }

    public R getRow(String rowName)
    {
        return castToRow(row(rowName));
    }

    private E newEntity(){
        try {
            return entityClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw exception("Can't instantiate %s. You must have empty constructor to do this",
                    entityClass.getSimpleName());
        }
    }

    private E rowToEntity(MapArray<String, ICell> row) {
        E entity = newEntity();
        if (row == null)
            return entity;

        Field[] fields = entity.getClass().getFields();

        row.pairs.forEach(entry
            -> setEntityField(entity, fields, entry.key, entry.value.getText()));
        return entity;
    }

    public List<E> entities(String... colNames){
        return select(colNames,
                colName -> rowToEntity(new MapArray<>(size(),
                        i -> columns.getColumn(colName).get(i))));
    }

    public E entity(int rowNum){
        return rowToEntity(rows.getRow(rowNum));
    }

    public E entity(String value, Column column){
        return rowToEntity(row(value, column));
    }

    public E entity(String rowName){
        return rowToEntity(rows.getRow(rowName));
    }

    public List<E> all(){
        return select(rows.get().values(), this::rowToEntity);
    }

    private void setEntityField(E entity, Field[] fields, String fieldName, String value)
    {
        setField(entity, fields, fieldName, field -> convertStringToType(value, field));
    }

    public int size() {
        return rows.count();
    }

    public boolean contains(Object o) {
        return allCells.contains(o);
    }

    public Iterator<E> iterator() {
        return all().iterator();
    }

    public Object[] toArray() {
        return all().toArray();
    }

    public <T1> T1[] toArray(T1[] a) {
        return all().toArray(a);
    }

    public boolean add(E t) {
        throw new UnsupportedOperationException();
    }

    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    public boolean containsAll(Collection<?> c) {
        return all().containsAll(c);
    }

    public boolean addAll(Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    public boolean addAll(int index, Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    public E get(int index) {
        return entity(index);
    }

    public E set(int index, E element) {
        throw new UnsupportedOperationException();
    }

    public void add(int index, E element) {
        throw new UnsupportedOperationException();
    }

    public E remove(int index) {
        throw new UnsupportedOperationException();
    }

    public int indexOf(Object o) {
        return all().indexOf(o);
    }

    public int lastIndexOf(Object o) {
        return all().lastIndexOf(o);
    }

    public ListIterator<E> listIterator() {
        return all().listIterator();
    }

    public ListIterator<E> listIterator(int index) {
        return all().listIterator(index);
    }

    public List<E> subList(int fromIndex, int toIndex) {
        return all().subList(fromIndex, toIndex);
    }
}
