<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

    <xsl:template match="/">
        <html>
            <head>
                <title>word records</title>
                <style type="text/css">
                    <![CDATA[
                        .hidden{
                            display:none;
                        }
                    ]]>
                </style>
                <script type="text/javascript">
                    <![CDATA[
                        function displaySentence() {
                            var port = document.getElementById("port").innerHTML;
                            var e = window.event.srcElement.parentNode;
                            e.style = "color:red";
                            var hash = e.childNodes[2].innerHTML;
                            var word = e.childNodes[3].innerHTML;
                            var xmlhttp = new XMLHttpRequest();
                            xmlhttp.open("GET", "http://localhost:" + port + "/updatesentence?word=" + word + "&hash=" + hash, true);
                            xmlhttp.send();
                        }
                    ]]>
                </script>
            </head>
            <body>
                <xsl:for-each select="wordlist/word_sublist">
                    <div class="word_sublist">
                        <h3 class="state"><xsl:value-of select="@type"/></h3>
                        <xsl:for-each select="word">
                            <div class="word_record">
                                <div class="word_content">
                                    <span><xsl:value-of select="content"/></span>
                                </div>
                                <ul class="sentences">
                                    <xsl:for-each select="sentence">
                                        <li class="sentence_item" onclick="displaySentence()">
                                            <span class="sentence" >
                                                <xsl:value-of select="sentenceContent"/>
                                            </span>
                                            <span class="hidden">
                                                <xsl:value-of select="sentenceHash"/>
                                            </span>
                                            <span class="hidden">
                                                <xsl:value-of select="../content"/>
                                            </span>
                                        </li>
                                    </xsl:for-each>
                                </ul>
                            </div>
                        </xsl:for-each>
                    </div>
                </xsl:for-each>
                <div class="hidden">
                    <span id="port"><xsl:value-of select="wordlist/@port"/></span>
                </div>
            </body>
        </html>
    </xsl:template>

</xsl:stylesheet>