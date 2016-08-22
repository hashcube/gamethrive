<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:android="http://schemas.android.com/apk/res/android"  xmlns:amazon="http://schemas.amazon.com/apk/res/android">

  <xsl:param name="packageName"></xsl:param>
  <xsl:param name="googleProjectNo"></xsl:param>
  <xsl:param name="onesignalAppID"></xsl:param>

  <xsl:template match="meta-data[@android:name='googleProjectNo']">
    <meta-data android:name="googleProjectNo" android:value="\ {$googleProjectNo}"/>
  </xsl:template>

  <xsl:template match="meta-data[@android:name='googleProjectNo']">
    <meta-data android:name="googleProjectNo" android:value="\ {$googleProjectNo}"/>
  </xsl:template>

  <xsl:template match="meta-data[@android:name='onesignalAppID']">
    <meta-data android:name="onesignalAppID" android:value="{$onesignalAppID}"/>
  </xsl:template>

  <xsl:template match="permission[@android:name='c2d_message']">
    <permission android:protectionLevel="signature" android:name="{$packageName}.permission.C2D_MESSAGE"/>
  </xsl:template>

  <xsl:template match="uses-permission[@android:name='packageNamePermissionC2D']">
    <uses-permission android:name="{$packageName}.permission.C2D_MESSAGE"/>
  </xsl:template>

  <xsl:template match="permission[@android:name='adm_message']">
		<permission android:protectionLevel="signature" android:name="{$packageName}.permission.RECEIVE_ADM_MESSAGE"/>
  </xsl:template>

  <xsl:template match="uses-permission[@android:name='packageNamePermissionRECEIVE']">
    <uses-permission android:name="{$packageName}.permission.RECEIVE_ADM_MESSAGE" />
  </xsl:template>

  <xsl:template match="category[@android:name='packageName']">
    <category android:name="{$packageName}"/>
  </xsl:template>

 <!--    <xsl:strip-space elements="*" />-->
  <xsl:output indent="yes" />
  <xsl:template match="comment()" />
  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" />
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
