<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template match="/">
        <html>
            <head>
                <title>word records</title>
            </head>
            <body>
                <xsl:for-each select="wordlist/word_sublist">
                    <div class="word_sublist">
                        <xsl:for-each select="word">
                            <div class="word_record">
                                <div class="word_content">
                                    <span><xsl:value-of select="content"/></span>
                                </div>
                                <ul class="sentences">
                                    <xsl:for-each select="sentence">
                                        <li class="sentence_item">
                                            <span class="sentence">
                                                <xsl:value-of select="."/>
                                            </span>
                                        </li>
                                    </xsl:for-each>
                                </ul>
                            </div>
                        </xsl:for-each>
                    </div>
                </xsl:for-each>
            </body>
        </html>
    </xsl:template>

</xsl:stylesheet>