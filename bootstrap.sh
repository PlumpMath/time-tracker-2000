#!/usr/bin/env bash
cd $HOME 
VAGRANT_HOME=/home/vagrant

# APT-GET
apt-get update
apt-get install -y curl htop git openjdk-7-jre-headless vim zsh texlive

# ZSH
chsh -s /bin/zsh vagrant
git clone git://github.com/robbyrussell/oh-my-zsh.git $VAGRANT_HOME/.oh-my-zsh
cp $VAGRANT_HOME/.oh-my-zsh/templates/zshrc.zsh-template $VAGRANT_HOME/.zshrc
chown -R vagrant:vagrant $VAGRANT_HOME/.oh-my-zsh
chown vagrant:vagrant $VAGRANT_HOME/.zshrc
echo export PATH="$PATH:$HOME/bin" >> $VAGRANT_HOME/.zshrc

# LEIN
mkdir $VAGRANT_HOME/bin
chown vagrant:vagrant $VAGRANT_HOME/bin
wget https://raw.github.com/technomancy/leiningen/stable/bin/lein -P $VAGRANT_HOME/bin
chmod 755 $VAGRANT_HOME/bin
