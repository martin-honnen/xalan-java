<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                xmlns:math="http://www.w3.org/2005/xpath-functions/math"
				xmlns:fn0="http://fn0"
                exclude-result-prefixes="xs math fn0"				
                version="3.0">
                
   <!-- Author: mukulg@apache.org -->                

   <!-- use with test2.xml -->
   
   <!-- An XSLT test case to test, XPath path expression
        having a function call suffix.
   -->				

   <xsl:output method="xml" indent="yes"/>

   <xsl:template match="/info">
	  <result>
	     <xsl:for-each select="a/@val/fn0:sqr(xs:double(.))">
		   <value inp="{math:sqrt(xs:double(.))}" sqr="{.}"/>
		 </xsl:for-each>
      </result>	  
   </xsl:template>
   
   <!-- An XSL stylesheet function, to find square of 
        the function argument. -->
   <xsl:function name="fn0:sqr" as="xs:double">
      <xsl:param name="num" as="xs:double"/>
	  <xsl:sequence select="$num * $num"/>
   </xsl:function>
   
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