/*
 * Copyright 2014 Azige.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.azige.mages;

import javax.script.ScriptEngine;
import javax.script.ScriptException;

/**
 *
 * @author Azige
 */
public class GroovyPreFilter implements GroovyFilter{

    private ScriptEngine engine;

    @Override
    public String filter(String source){
        try{
            return engine.eval(source).toString();
        }catch (ScriptException ex){
            throw new MagesException(ex);
        }
    }

    @Override
    public void setEngine(ScriptEngine engine){
        this.engine = engine;
    }
}
