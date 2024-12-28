<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"                
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
				xmlns:array="http://www.w3.org/2005/xpath-functions/array"
                exclude-result-prefixes="xs array"
                version="3.0">
                
  <!-- Author: mukulg@apache.org -->
  
  <!-- An XSLT test case to test, passing literal array as argument 
       with XPath dynamic function call. -->                

  <xsl:output method="xml" indent="yes"/>

  <xsl:template match="/">
      <result>
	     <xsl:variable name="func1" select="function ($arr as array(*)) as xs:integer { array:size($arr) }"/>
	     <one>            
		    <xsl:value-of select="$func1([1,2,3,4,5])"/>
		 </one>
		 <xsl:variable name="func2" select="function ($arr1 as array(*), $arr2 as array(*)) as xs:integer 
		                                                          { array:size($arr1) + array:size($arr2) }"/>
	     <two>            
		    <xsl:value-of select="$func2([1,2,3,4,5], [1,2])"/>
		 </two>
		 <xsl:variable name="func3" select="function ($arr1 as array(*), $val as xs:integer, $arr2 as array(*)) as xs:integer 
		                                                          { (array:size($arr1) + array:size($arr2)) * $val }"/>
	     <three>            
		    <xsl:value-of select="$func3([1,2,3,4,5], 10, [1,2])"/>
		 </three>
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