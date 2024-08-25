<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"                         				
			    version="3.0">
			    
  <!-- Author: mukulg@apache.org -->
  
  <!-- An XSLT stylesheet test case, to test XPath 3.1 nested 
       'if' expressions. -->			    
  
  <xsl:output method="xml" indent="yes"/>
  
  <xsl:template match="/">
     <result>
	     <one>
		   <xsl:variable name="var1" select="if (1 = 1) then (if (2 = 2) then 'hello' else 'hi') 
		                                                  else (if (2 = 2) then 'great' else 'thanks')"/>
		   <xsl:value-of select="$var1"/>
		 </one>
		 <two>
		   <xsl:variable name="var1" select="if (1 = 2) then (if (2 = 2) then 'hello' else 'hi') 
		                                                  else (if (2 = 2) then 'great' else 'thanks')"/>
		   <xsl:value-of select="$var1"/>
		 </two>
		 <three>
		   <xsl:variable name="var1" select="if (1 = 2) then (if (2 = 2) then 'hello' else 'hi') 
		                                                  else (if (2 = 3) then 'great' else (if (1 = 1) then 'str1' else 'str2'))"/>
		   <xsl:value-of select="$var1"/>
		 </three>
		 <four>
		   <xsl:variable name="var1" select="if (1 = 2) then (if (2 = 2) then 'hello' else 'hi') 
		                                                  else (if (2 = 3) then 'great' else (if (1 = 2) then 'str1' else 'str2'))"/>
		   <xsl:value-of select="$var1"/>
		 </four>  
      </result>
  </xsl:template>
  
  <!--
      * Licensed to the Apache Software Foundation (ASF) under one
      * or more contributor license agreements. See the NOTICE file
      * distributed with this work for additional information
      * regarding copyright ownership. The ASF licenses this file
      * to you under the Apache License, Version 2.0 (the  "License");
      * you may not use this file except in compliance with the License.
      * You may obtain a copy of the License at
      *
      *     http://www.apache.org/licenses/LICENSE-2.0
      *
      * Unless required by applicable law or agreed to in writing, software
      * distributed under the License is distributed on an "AS IS" BASIS,
      * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
      * See the License for the specific language governing permissions and
      * limitations under the License.
   -->
  
</xsl:stylesheet>
