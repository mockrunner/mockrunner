/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mockrunner.mock.jdbc;

import com.mockrunner.jdbc.ParameterUtil;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Administrator
 */
public class MockParameterMap extends HashMap<ParameterReference, Object> {

//    Map<Integer, Object> indexedParameters;
//    Map<String, Object> namedParameters;
    
    public MockParameterMap() {
        super();
    }

    public MockParameterMap(MockParameterMap other){
        super(other);
    }

    public MockParameterMap(Object[] parameterArray){
        super();
        for(int ii = 0; ii < parameterArray.length; ii++){
            put(ii + 1, parameterArray[ii]);
        }
    }
    
    public MockParameterMap(List<Object> parameterList){
        super();
        for(int ii = 0; ii < parameterList.size(); ii++){
            put(ii + 1, parameterList.get(ii));
        }
    }
    
    public void put(int index, Object value){
        put(new ParameterIndex(index), value);
    }
    
    public Object get(int index){
        return get(new ParameterIndex(index));
    }
    
    public boolean contains(int index){
      return containsKey(new ParameterIndex(index));
    }
    
//    @Deprecated
//    public void put(Integer index, Object value){
//        put(new ParameterIndex(index), value);
//    }
//    
//    @Deprecated
//    public Object get(Integer index){
//        return get(new ParameterIndex(index));
//    }
//    
//    @Deprecated
//    public Object remove(Integer index){
//        return remove(new ParameterIndex(index));
//    }
    
    public void put(String name, Object value){
        put(new ParameterName(name), value);
    }
    
    public Object get(String name){
        return get(new ParameterName(name));
    }
    
    public boolean contains(String name){
      return containsKey(new ParameterName(name));
  }
    
    public Object remove(int index){
        return remove(new ParameterIndex(index));
    }
    
    public Object remove(String name){
        return remove(new ParameterName(name));
    }
    
    public boolean doParameterMatch(MockParameterMap actualParameters, boolean exactMatchParameter)
    {
        if(exactMatchParameter)
        {
            if(actualParameters.size() != this.size()) return false;
            for(ParameterReference currentKey : actualParameters.keySet()){
                Object expectedObject = this.get(currentKey);
                if(!ParameterUtil.compareParameter(actualParameters.get(currentKey), expectedObject))
                {
                    return false;
                }
            }
            return true;
        }
        else
        {
            for(ParameterReference currentKey : this.keySet()){
                Object actualObject = actualParameters.get(currentKey);
                if(!ParameterUtil.compareParameter(actualObject, this.get(currentKey)))
                {
                    return false;
                }
            }
            return true;
        }
    }    
    
}
