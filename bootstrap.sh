#!/usr/bin/env bash
cd $HOME 
VAGRANT_HOME=/home/vagrant

# APT-GET
apt-get update
apt-get install -y curl htop git openjdk-7-jre-headless vim zsh \
  texlive texlive-base texlive-binaries texlive-common texlive-doc-base \
  texlive-extra-utils texlive-font-utils texlive-fonts-recommended \
  texlive-fonts-recommended-doc texlive-generic-recommended texlive-humanities \
  texlive-humanities-doc texlive-latex-base texlive-latex-base-doc texlive-latex-extra \
  texlive-latex-extra-doc texlive-latex-recommended texlive-latex-recommended-doc \
  texlive-luatex texlive-pictures texlive-pictures-doc texlive-pstricks \
  texlive-pstricks-doc texlive-publishers texlive-publishers-doc

# ZSH
chsh -s /bin/zsh vagrant
git clone git://github.com/robbyrussell/oh-my-zsh.git $VAGRANT_HOME/.oh-my-zsh
cp $VAGRANT_HOME/.oh-my-zsh/templates/zshrc.zsh-template $VAGRANT_HOME/.zshrc
chown -R vagrant:vagrant $VAGRANT_HOME/.oh-my-zsh
chown vagrant:vagrant $VAGRANT_HOME/.zshrc

# LEIN
mkdir $VAGRANT_HOME/bin
chown vagrant:vagrant $VAGRANT_HOME/bin
cd $VAGRANT_HOME/bin
wget https://raw.github.com/technomancy/leiningen/stable/bin/lein
chmod 755 $VAGRANT_HOME/bin/lein
echo "export PATH=$PATH:/home/vagrant/bin" >> $VAGRANT_HOME/.zshrc
