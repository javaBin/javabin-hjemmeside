<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:html="http://www.w3.org/1999/xhtml">
	<xsl:output omit-xml-declaration="yes" indent="yes"/>
	
	<xsl:template match="/xml">
		<xsl:apply-templates select="@*|node()"/>
	</xsl:template>

	<xsl:template match="*|@*">
		<xsl:copy>
			<xsl:apply-templates select="@*|node()"/>
		</xsl:copy>
	</xsl:template>
	<xsl:template match="html:a[starts-with(@href, 'http://wiki.java.no/display/forside/')]/@href" >
			<xsl:attribute name="href">
           		<xsl:value-of select="substring(., 36)" />
			</xsl:attribute>
	</xsl:template>
	<xsl:template match="html:img[starts-with(@src, '/download/attachments/')]/@src" >
			<xsl:attribute name="src">http://wiki.java.no<xsl:value-of select="." /></xsl:attribute>
	</xsl:template>
</xsl:stylesheet>
