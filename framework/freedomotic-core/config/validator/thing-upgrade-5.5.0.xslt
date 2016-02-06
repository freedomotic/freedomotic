<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template name="string-replace">
        <xsl:param name="text" />
        <xsl:param name="replace" />
        <xsl:param name="by" />
        <xsl:choose>
            <xsl:when test="contains($text, $replace)">
                <xsl:value-of select="substring-before($text,$replace)" />
                <xsl:value-of select="$by" />
                <xsl:value-of select="substring-after($text,$replace)" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$text" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="it.freedomotic.model.object.EnvObject">
        <object version="5.6.0">
   	   <xsl:apply-templates select="@*|node()" />
        </object>
    </xsl:template>

    <xsl:template match="object">
        <object version="5.6.0">
   	   <xsl:apply-templates select="@*|node()" />
        </object>
    </xsl:template>


    <xsl:template match="behaviors/*[starts-with(name(), 'it.freedomotic.model.object')]">
        <xsl:element name="com.freedomotic.model.object{substring-after(name(),'it.freedomotic.model.object')}">
   	   <xsl:apply-templates select="@*|node()" />
       </xsl:element>
    </xsl:template>

    <xsl:template match="hierarchy">
        <xsl:element name="hierarchy">
            <xsl:variable name="tobereplaced" select="."/>   
            <xsl:variable name="a">
                <xsl:call-template name="string-replace">
                    <xsl:with-param name="text" select="$tobereplaced" />
                    <xsl:with-param name="replace" select="'it.freedomotic'" />
                    <xsl:with-param name="by" select="'com.freedomotic'" />
                </xsl:call-template>    
            </xsl:variable>
       <!--     <xsl:variable name="b">
                <xsl:call-template name="string-replace">
                    <xsl:with-param name="text" select="$a" />
                    <xsl:with-param name="replace" select="'Gate'" />
                    <xsl:with-param name="by" select="'GenericGate'" />
                </xsl:call-template>     
            </xsl:variable>
            <xsl:variable name="c">
                <xsl:call-template name="string-replace">
                    <xsl:with-param name="text" select="$b" />
                    <xsl:with-param name="replace" select="'Person'" />
                    <xsl:with-param name="by" select="'GenericPerson'" />
                </xsl:call-template>     
            </xsl:variable> -->
            <xsl:variable name="d">
                <xsl:call-template name="string-replace">
                    <xsl:with-param name="text" select="$a" /><!-- -->
                    <xsl:with-param name="replace" select="'freedomotic.objects'" />
                    <xsl:with-param name="by" select="'freedomotic.things'" />
                </xsl:call-template>     
            </xsl:variable>
            <xsl:value-of select="$d"/>
        </xsl:element>
    </xsl:template> 

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()" />
        </xsl:copy>
    </xsl:template>
</xsl:stylesheet>
