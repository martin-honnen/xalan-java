<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:map="http://www.w3.org/2005/xpath-functions/map"
                exclude-result-prefixes="xs map"				
                version="3.0">
                
   <!-- Author: mukulg@apache.org -->                

   <!-- use with test2.xml -->
   
   <!-- An XSLT test case to test, XPath path expression
        having a function call suffix.
   -->				

   <xsl:output method="xml" indent="yes"/>
   
   <xsl:variable name="map1" select="map {2 : 'hello', 4 : 'there', 6 : 'how', 8 : 'are', 10 : 'you'}" as="map(*)"/>

   <xsl:template match="/info">
	  <result>
	     <xsl:for-each select="a/@val/map:get($map1, xs:integer(.))">
		   <word>
		      <xsl:value-of select="."/>
		   </word>
		 </xsl:for-each>
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