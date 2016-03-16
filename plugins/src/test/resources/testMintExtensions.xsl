<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:func="http://www.socialhistoryservices.org/">
    <xsl:output indent="yes"/>

    <xsl:template match="/">
        <pid>
            <xsl:value-of select="func:getQuickPidRequest('12345', ., 'http://www.osaarchivum.org')" />
        </pid>
        <uuid>
            <xsl:value-of select="func:makeuuid()" />
        </uuid>
    </xsl:template>
</xsl:stylesheet>