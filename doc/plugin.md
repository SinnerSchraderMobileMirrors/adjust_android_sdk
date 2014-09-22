# Plugins

The plugin mechanism allows to add optional functionality to the Adjust SDK without changing the current implementation. Just add one of the plugins from the `plugin` folder to the `Adjust → src → com → adjust → sdk → plugin` and it's functionality will be automatically added.

## Email sha-1

The `EmailUtil` plugin allow to collect the sha-1 of the device's primary email. To access this information, you need to add the following permission to the `AndroidManifest.xml` of your app:
````
<uses-permission android:name="android.permission.GET_ACCOUNTS" />
````
You can also add optionally a string to be concatenated with the email before is hashed with the `sha-1` algorithm. Add the string in a `meta-data` tag inside the `application` tag, as follows:
```
<meta-data android:name="AdjustSalt"    android:value="salt example" />
```
