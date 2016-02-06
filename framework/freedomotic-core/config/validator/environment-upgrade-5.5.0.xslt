<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()" />
        </xsl:copy>
    </xsl:template>

    <xsl:template match="it.freedomotic.model.environment.Zone">
        <zone>
   	   <xsl:apply-templates select="@*|node()" />
	</zone>
    </xsl:template>
    <xsl:template match="it.freedomotic.model.environment.Environment">
        <environment version="5.6.0">
	   <xsl:apply-templates select="@*|node()" />
	</environment>
    </xsl:template>
    <xsl:template match="environment">
        <environment version="5.6.0">
	   <xsl:apply-templates select="@*|node()" />
	</environment>
    </xsl:template>
</xsl:stylesheet>
