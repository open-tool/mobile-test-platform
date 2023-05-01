yum clean all
rm -rfv /var/cache/yum /var/log/yum.log
find /etc -name '*.rpmnew' -delete -o -name '*.rpmsave' -delete
