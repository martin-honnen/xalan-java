<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xs="http://www.w3.org/2001/XMLSchema"
                exclude-result-prefixes="xs"
                version="3.0">
                
    <!-- Author: mukulg@apache.org -->
   
    <!-- An XSLT stylesheet test, to test the XPath 3.1 function 
         fn:min. Within this stylesheet, we find the minimum
         value within a sequence of xs:yearMonthDuration values.
    -->                 
    
    <xsl:output method="xml" indent="yes"/>
        
    <xsl:variable name="seq1" select="(xs:yearMonthDuration('P1Y3M'), xs:yearMonthDuration('P2Y2M'), 
                                                     xs:yearMonthDuration('P3M'), xs:yearMonthDuration('P1Y5M'))"/>
        
    <xsl:template match="/">
       <result>
          <xsl:value-of select="min($seq1)"/>
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