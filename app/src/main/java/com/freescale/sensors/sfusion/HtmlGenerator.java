/*
Copyright (c) 2013, 2014, Freescale Semiconductor, Inc.
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright
   notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright
   notice, this list of conditions and the following disclaimer in the
   documentation and/or other materials provided with the distribution.
 * Neither the name of Freescale Semiconductor, Inc. nor the
   names of its contributors may be used to endorse or promote products
   derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL FREESCALE SEMICONDUCTOR, INC. BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

// This class provides utility functions for creation of HTML output files. 
// It is used for generation of stats output report.

package com.freescale.sensors.sfusion;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.net.Uri;
import android.os.Environment;

public class HtmlGenerator {
	public String fileName = null;
	public File file = null;
	public String fullFileName = null;
	public Uri uri = null;
	public boolean ok = true;
	private BufferedOutputStream outputStream = null;

	HtmlGenerator(A_FSL_Sensor_Demo demo) {
		String defaultStatsFileName  = demo.getString(R.string.statsOutputFile);
		fileName = demo.myPrefs.getString("statsFileName", defaultStatsFileName);
	}
	private boolean outputFileAvailable() {
		boolean sts = false;
		if (outputStream!=null) {
			sts = true;
		} else if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {  // do we have external storage
			if (fullFileName==null) {  // have we previously gotten the external file path
				file = MyUtils.getFile(fileName);
				fullFileName = file.getAbsolutePath();
				A_FSL_Sensor_Demo.write(true, "\n\nHTML output file stored to " + fullFileName + " on your device.\n");
				uri = Uri.fromFile(file);
				sts = true;
			}			
			if ((outputStream==null)&&(fullFileName!=null)) {
				try {
					outputStream = new BufferedOutputStream(new FileOutputStream(fullFileName));
					sts = true;					
				} catch (FileNotFoundException e){
					outputStream=null;
					sts = false;
					e.printStackTrace();
				}
			}
		} else {
			outputStream=null;
			sts = false;
		}
		return(sts);
	}
	public boolean write(String s) {
		boolean sts = false;
		if (outputFileAvailable()) {
			try {
				outputStream.write(s.getBytes());
				sts = true;
			} catch (IOException e) {				
				e.printStackTrace();
			}
		}
		ok = ok & sts;
		return(sts);
	}
	public void close() {
		if (outputStream!=null) {
			try {
				outputStream.close();
				outputStream=null; // required to get back into file initiation later if needed
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	void dumpCss() {
		write("<style>\n");
		write("<!--\n");
		write("pre { font-size: 10pt; }");
		write("body { font-size: 10pt; }");
		write("table \n");
		write("{\n");
		write("  border-collapse: collapse;\n");
		write("  border: 1 solid black;\n");
		write("}\n");
		write("td\n");
		write("{\n");
		write("  border: 1 solid black;\n");
		write("  padding: 2px;\n");
		write("  text-align: right;\n");
		write("}\n");
		write("thead\n");
		write("{\n");
		write("  background: yellow;\n");
		write("  border: 1 solid black;\n");
		write("}\n");
		write("-->\n");
		write("</style>\n");
	}

	public void start(String title) {
		write("<HTML>\n<HEAD>\n");
		dumpCss();
		write(String.format("<TITLE>%s</TITLE>\n", title));
		write("</HEAD><BODY>\n");
	}
	public void end() {
		 write("</BODY></HTML>\n");
	}
	public void h1(String s) {
		write_tag("h1", s);
	}
	public void h2(String s) {
		write_tag("h2", s);
	}
	public void h3(String s) {
		write_tag("h3", s);
	}
	public void start_ol() {
		write("<OL>\n");
	}
	public void end_ol() {
		write("</OL>\n");
	}
	public void start_ul() {
		write("<UL>\n");
	}
	public void end_ul() {
		write("</UL>\n");
	}
	public void li(String s) {
		write_tag("LI", s);
	}
	public void hr() {
		write("<HR>\n");
	}
	public void pre(String s) {
		write_tag("PRE", s);
	}
	public void para() {
		write("<P>");
	}
	public void para(String s) {
		write("<P>"+s);
	}
	public void start_thead() {
		write("<THEAD>\n");
	}
	public void end_thead() {
		write("</THEAD>\n");
	}
	public void thead(String [] slist) {
		start_thead();
		row(slist);
		end_thead();
	}
	public void start_tbody() {
		write("<TBODY>\n");
	}
	public void end_tbody() {
		write("</TBODY>\n");
	}
	public void start_table() {
		write("<TABLE>\n");
	}
	public void end_table() {
		write("</TABLE>\n");
	}
	public void strong(String s) {
		write_tag("Strong", s);
	}
	public void row(String [] sList) {
		write("<TR>\n");
		for (String s : sList) {
			td(s);
		}
		write("</TR>\n");
	}
	public void row(String q, float v, float min, float mean, float max, float stdDev, String units, float perRtHz) {
		String vS = String.format("%10.4f", v);
		String minS = String.format("%10.4f", min);
		String meanS = String.format("%10.4f", mean);
		String maxS = String.format("%10.4f", max);
		String stdDevS = String.format("%10.6f", stdDev);
		String perRtHzS = String.format("%10.6f", perRtHz);
		write("<TR>\n");
		td(q); td(vS); td(minS); td(meanS); td(maxS); td(stdDevS); td(units); 
		if (perRtHz>=0) {
			td(perRtHzS);
		} else {
			td("-");
		}
		write("</TR>\n");
	}
	public void td(String s) {
		write_tag("TD", s);
	}
	private void write_tag(String tag, String s) {
		write(String.format("<%s>%s</%s>\n", tag, s, tag));
	}	
}
