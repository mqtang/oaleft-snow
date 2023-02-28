# OALEFT Snow

## Requirements

- Java 17
- Maven 3.8.5

## GPG

[GPG Requirements](https://central.sonatype.org/publish/requirements/gpg/)

```shell
gpg --version
gpg --gen-key
gpg --list-keys
gpg --list-signatures
gpg --list-signatures --keyid-format 0xshort
# generate and verify
gpg -ab myfile.java
gpg --verify myfile.java.asc 
# publish key
gpg --keyserver keyserver.ubuntu.com --send-keys CA925CD6C9E8D064FF05B4728190C4130ABA0F98
gpg --keyserver keys.openpgp.org --send-keys CA925CD6C9E8D064FF05B4728190C4130ABA0F98
# use key
gpg --keyserver keys.openpgp.org --recv-keys CA925CD6C9E8D064FF05B4728190C4130ABA0F98
```

