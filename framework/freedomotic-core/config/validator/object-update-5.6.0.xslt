<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="object">
 <object version="5.6.0">
  <xsl:apply-templates />
 </object>
</xsl:template>

<xsl:template match="hierarchy">
 <hierarchy>
  <xsl:variable name="a" select="replace(.,'it.freedomotic', 'com.freedomotic')"/>
  <xsl:variable name="b" select="replace($a,'Gate', 'GenericGate')"/>
  <xsl:variable name="c" select="replace($b,'Person', 'GenericPerson')"/>
  <xsl:value-of select="$c"/>
 </hierarchy>
</xsl:template> 

<xsl:template match="*">
 <xsl:copy-of select="."/>
</xsl:template>

</xsl:stylesheet>
