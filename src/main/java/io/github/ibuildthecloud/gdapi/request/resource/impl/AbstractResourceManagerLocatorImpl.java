package io.github.ibuildthecloud.gdapi.request.resource.impl;

import io.github.ibuildthecloud.gdapi.factory.SchemaFactory;
import io.github.ibuildthecloud.gdapi.request.ApiRequest;
import io.github.ibuildthecloud.gdapi.request.resource.ResourceManager;
import io.github.ibuildthecloud.gdapi.request.resource.ResourceManagerFilter;
import io.github.ibuildthecloud.gdapi.request.resource.ResourceManagerLocator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

public abstract class AbstractResourceManagerLocatorImpl implements ResourceManagerLocator {

    SchemaFactory schemaFactory;
    Map<Object, ResourceManager> cached = new ConcurrentHashMap<Object, ResourceManager>();

    @Override
    public ResourceManager getResourceManager(ApiRequest request) {
        if ( request.getType() == null )
            return null;

        return getResourceManagerByType(request.getType());
    }

    @Override
    public String getType(Class<?> type) {
        return schemaFactory.getSchemaName(type);
    }

    protected abstract ResourceManager getDefaultResourceManager();

    protected abstract ResourceManager getResourceManagersByTypeInternal(String type);

    protected abstract List<ResourceManagerFilter> getResourceManagerFiltersByTypeInternal(String type);

    @Override
    public ResourceManager getResourceManagerByType(String type) {
        ResourceManager rm = cached.get(type);
        if ( rm != null ) {
            return rm;
        }

        rm = getResourceManagersByTypeInternal(type);

        if ( rm == null ) {
            rm = getDefaultResourceManager();
        }

        if ( rm == null ) {
            return rm;
        }

        List<ResourceManagerFilter> filters = getResourceManagerFiltersByTypeInternal(type);
        if ( filters == null ) {
            return rm;
        }

        rm = wrap(filters, rm);
        cached.put(type, rm);

        return rm;
    }

    protected ResourceManager wrap(List<ResourceManagerFilter> filters, ResourceManager resourceManager) {
        if ( filters.size() == 0 ) {
            return resourceManager;
        }

        List<ResourceManagerFilter> subList = new ArrayList<ResourceManagerFilter>();
        return new FilteredResourceManager(filters.get(0), wrap(subList, resourceManager));
    }

    protected void add(Map<String, List<ResourceManagerFilter>> filters, String key, ResourceManagerFilter filter) {
        List<ResourceManagerFilter> list = filters.get(key);
        if ( list == null ) {
            list = new ArrayList<ResourceManagerFilter>();
            filters.put(key, list);
        }

        list.add(filter);
    }

    public SchemaFactory getSchemaFactory() {
        return schemaFactory;
    }

    @Inject
    public void setSchemaFactory(SchemaFactory schemaFactory) {
        this.schemaFactory = schemaFactory;
    }

}