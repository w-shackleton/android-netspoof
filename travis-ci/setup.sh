sudo apt-get -qq update
sudo apt-get -qq -y install openjdk-7-jdk

wget https://dl.google.com/android/android-sdk_r24.0.2-linux.tgz
wget http://dl.google.com/android/ndk/android-ndk-r9-linux-x86_64.tar.bz2

tar xf android-sdk_r22.0.1-linux_4travis.tar.bz2
tar xf android-ndk-r9-linux-x86_64.tar.bz2

mv android-ndk-r9 android-ndk
