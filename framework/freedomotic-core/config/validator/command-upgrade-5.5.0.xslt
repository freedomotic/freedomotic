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
   <xsl:template match="it.freedomotic.reactions.Command">
        <command version="5.6.0">
            <xsl:apply-templates select="@*|node()" />
        </command>
    </xsl:template>

    <xsl:template match="command">
        <command version="5.6.0">
            <xsl:apply-templates select="@*|node()" />
        </command>
    </xsl:template>

    <xsl:template match="*">
        <xsl:copy-of select="."/>
    </xsl:template>

</xsl:stylesheet>
