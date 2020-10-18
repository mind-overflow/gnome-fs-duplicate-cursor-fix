# GNOME Fractional Scaling duplicate cursor fix  
Fix Ubuntu/GNOME's "duplicate cursor" fractional scaling bug.  

# How to install

1. **Enable X11 fractional scaling:**
```
gsettings set org.gnome.mutter experimental-features "['x11-randr-fractional-scaling']"
```

2. **Configure your monitors accordingly:**
- Go to "System Settings" -> "Monitors" and move/scale them to your preference.

3. **Install Java:**
```
sudo apt install default-jre
```

4. **Download latest version of .JAR file from releases:**  
https://github.com/mind-overflow/gnome-fs-duplicate-cursor-fix/releases/latest

5. **Create a directory in your home folder and put the JAR file inside of it:**
```
mkdir ~/screen-scaling-fixer/
mv ScreenScalingFixer*.jar ~/screen-scaling-fixer/ScreenScalingFixer.jar
```

6. **Create a startup script:**
```
echo "sh -c \"cd ~/screen-scaling-fixer/ && java -jar ~/screen-scaling-fixer/ScreenScalingFixer.jar\"" > ~/screen-scaling-fixer/start.sh
chmod +x ~/screen-scaling-fixer/start.sh
```

7. **Enable the startup script:**
```
printf \
"[Desktop Entry] \n\
Name=fs-fixer \n\
GenericName=fs-fixer \n\
Comment=fix gnome scaling duplicate cursor \n\
Exec=sh -c ~/screen-scaling-fixer/start.sh \n\
Terminal=false \n\
Type=Application \n\
X-GNOME-Autostart-enabled=true \n\
X-GNOME-Autostart-Delay=1 \n" > ~/.config/autostart/fractional-scaling-fix.desktop

chmod +x ~/.config/autostart/fractional-scaling-fix.desktop
```

8. **Run the JAR file and let config.yml generate:**
```
cd ~/screen-scaling-fixer
./start.sh
```

9. **Edit config.yml accordingly:**
```yaml
enable: false # set this to true, else the script won't run!
delay: 2000 # chose how long to wait after login before restarting GNOME.
monitor-connector: 'DisplayPort-1' # chose ONE monitor that requires fractional scaling to be enabled. Don't worry if you have multiple ones.
scale: 1.5 # set the desired fractional scale.
```

*Explanation:*
- `delay`: this is mandatory, because if you try restarting GNOME without any delay after login, it will not be restarted. Waiting a few seconds (~2) should be enough.
- `monitor-connector`: this is a monitor that requires fractional scaling. You should be able to gather its connector name by using command `xrandr`.
- `scale`: the scale of the chosen monitor. if you have multiple monitors, just chose one, as we only need to apply those changes to a single one to fix it for everyone else.

10. **Final step: log out and log back in.**

# How this works
The concept behind this fix is simple.  
If you enable the experimental fractional-scaling feature in eg. Ubuntu 20.04, you probably noticed that, upon login, a dead cursor is stuck on screen, and a secondary bigger one appears.  
This problem is fixed, however, if you switch back to 1.0x scale, apply, and then get back to your desired one.  
To automate this process, I developed a small Java open-source fix that automatically does it after logging in.  
You simply have to input your screen settings in config.yml, logout, re-login and you should notice your screen flashing twice.  
The dead cursor should disappear.  

