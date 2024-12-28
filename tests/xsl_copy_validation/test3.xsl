<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"                         				
			    version="3.0">
			    
  <!-- Author: mukulg@apache.org -->
   
  <!-- use with test2.xml -->
  
  <!-- An XSL stylesheet test case, that illustrates transforming 
       a particular XML input element among its sibling elements. -->			    
  
  <xsl:output method="xml" indent="yes"/>
  
  <xsl:template match="node() | @*">
     <xsl:copy>
	    <xsl:apply-templates select="node() | @*"/>
	 </xsl:copy>
  </xsl:template>
  
  <!-- An XSL stylesheet template, that copies an XML element with 
       xsl:copy shallow copy instruction, and adds three attributes (one 
       original and two new) to resulting element. -->
  <xsl:template match="a[@val = '3']">
    <xsl:copy>
	  <xsl:copy-of select="@val"/>
	  <xsl:attribute name="incr" select="100"/>
	  <xsl:attribute name="newVal" select="@val + 100"/>
	  <xsl:apply-templates/>
	</xsl:copy>
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