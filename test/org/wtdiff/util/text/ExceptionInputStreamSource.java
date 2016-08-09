/*
Copyright 2015 David Standish

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/
package org.wtdiff.util.text;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import org.wtdiff.util.ExceptionInputStream;
import org.wtdiff.util.text.InputStreamSource;

public class ExceptionInputStreamSource implements InputStreamSource {

    private String name;
    private long time;
    private String content;
    private int errorOffset;
    
    public ExceptionInputStreamSource(String name, long modTime,  String content, int errorOffset) {
        this.name = name;
        this.time = modTime;
        this.content = content;
        this.errorOffset = errorOffset;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ExceptionInputStream( new ByteArrayInputStream(content.getBytes()), errorOffset );
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getTime() {
        return time;
    }

}
