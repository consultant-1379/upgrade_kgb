from __future__ import with_statement
from fabric.api import local, settings, abort, run
from fabric.contrib.console import confirm


def display(fred):
    with settings(warn_only=True):
        tester="/home/orcha/display.bsh fred"
        result=run(tester)
        if result.failed:abort(result.return_code)


def slu_pre_upgrade_verification( server_type, path_to_sol, path_to_patch, mws_ip, test1):
    var1 = "/usr/local/bin/sudo /var/tmp/Liveupgrade/slu_pre_upgrade_verification.bsh " +  server_type + " " + mws_ip + " " + path_to_sol + " " + path_to_patch + " " + test1
    print (var1)
    run (var1)
    pass


def slu_pre_upgrade_verification_no_sudo( server_type, path_to_sol, path_to_patch, mws_ip, path_to_om="/tmp"):
    var1 = "/var/tmp/Liveupgrade/slu_pre_upgrade_verification.bsh " +  server_type + " " + mws_ip + " " + path_to_sol + " " + path_to_patch
    print (var1)
    run (var1)
    pass


def slu_pre_upgrade_tasks( server_type, path_to_sol, path_to_patch, mws_ip, path_to_om="/var/tmp"):
    var2 = "/usr/local/bin/sudo /var/tmp/Liveupgrade/slu_pre_upgrade_tasks.bsh " +  server_type + " " + mws_ip + " " + path_to_sol + " " + path_to_patch
    print (var2)
    run (var2)
    pass


def slu_upgrade_tasks( options, server_type, path_to_sol, path_to_patch, mws_ip, path_to_om="/var/tmp"):
    var2 = "/usr/local/bin/sudo /var/tmp/Liveupgrade/slu_upgrade_tasks.bsh " + server_type + " " + mws_ip + " " + path_to_sol + " " + path_to_patch
    #run(var2)


def slu_pre_upgrade_tasks_no_sudo( server_type, path_to_sol, path_to_patch, mws_ip, path_to_om="/var/tmp"):
    var2 = "/var/tmp/Liveupgrade/slu_pre_upgrade_tasks.bsh " +  server_type + " " + mws_ip + " " + path_to_sol + " " + path_to_patch
    run (var2)


def slu_upgrade_tasks( options, server_type, path_to_sol, path_to_patch, mws_ip, path_to_om="/var/tmp"):
    var2 = "/usr/local/bin/sudo /var/tmp/Liveupgrade/slu_upgrade_tasks.bsh " + options + " " + server_type + " " + mws_ip + " " + path_to_sol + " " + path_to_patch
    print (var2)
    run (var2)


def slu_upgrade_tasksno_sudo( server_type, path_to_sol, path_to_patch, mws_ip, path_to_om="/var/tmp"):
    var2 = "/usr/local/bin/sudo /var/tmp/Liveupgrade/slu_upgrade_tasks.bsh " + server_type + " " + mws_ip + " " + path_to_sol + " " + path_to_patch
    print (var2)
    run (var2)


def slu_post_upgrade_tasks( server_type, test1 ):
    var2 = "/usr/local/bin/sudo /var/tmp/Liveupgrade/slu_post_upgrade_tasks.bsh " + server_type + " " + test1
    run(var2)


def slu_reboot_tasks(options ):
    var2 = "/var/tmp/Liveupgrade/slu_reboot_tasks.bsh " +  options
    print (var2)
    run (var2)


def slu_post_reboot_tasks( options, number, server_type, test1 ):
    var2 = "/usr/local/bin/sudo /var/tmp/Liveupgrade/slu_post_reboot_tasks.bsh " + options + " " + number + " " +  server_type + " " + test1
    print (var2)
    run (var2)


def om_upgrade_tasks( options, server_type, mws_ip, path_to_om ):
    var2 = "/usr/local/bin/sudo /var/tmp/oam_utils/om_upgrade_tasks.bsh " +  " " + options + " " + server_type  + " " + mws_ip + " " + path_to_om
    print (var2)
    run (var2)


def orc_wait_for_reboot( options, test1, test2, server_type ):
    var2 = "/usr/local/bin/sudo /ericsson/orchestrator/bin/orc_wait_for_reboot.bsh " + options + " " + test1 + " " + test2 + " " + server_type
    print (var2)
    run (var2)


def pre_up_ver():
    run('/usr/local/bin/sudo /bin/bash -x /bashstuff/oss-rc/pre_up_ver.bsh')


def slu_upgrade_tasks_no_sudo( server_type, path_to_sol, path_to_patch, mws_ip, path_to_om="/var/tmp"):
    var2 = "/var/tmp/Liveupgrade/slu_upgrade_tasks.bsh " + server_type + " " + mws_ip + " " + path_to_sol + " " + path_to_patch
    print (var2)
    run (var2)


def pre_up_ver():
    run('/usr/local/bin/sudo /bin/bash -x /bashstuff/oss-rc/pre_up_ver.bsh')


def stop_ds():
    run('/ericsson/sdee/bin/stopDS.sh')


def start_ds():
    run('/ericsson/sdee/bin/startDS.sh')


def dsk_chk():
    run('/usr/local/bin/sudo /bashstuff/oss-rc/dsk_cap_wrapper.sh')


def admin_pre_verification( options, mws_ip, upgrade_dir, ossrc_media ):
    var2 = "/opt/ericsson/sck/bin/admin_pre_upgrade_verification.bsh " + options + " " + mws_ip + " " + upgrade_dir + " " + ossrc_media
    print (var2)
    run (var2)


def admin_pre_upgrade_tasks( options, mws_ip, upgrade_dir, ossrc_media ):
    var2 = "/opt/ericsson/sck/bin/admin_pre_upgrade_tasks.bsh " + options + " " + mws_ip + " " + upgrade_dir + " " + ossrc_media
    print (var2)
    run (var2)


def admin_reboot_tasks( options ):
    var2 = "/opt/ericsson/sck/bin/admin_reboot_tasks.bsh " +  options
    print (var2)
    run (var2)


def admin_final_pre_checks( options, mws_ip, path_to_orc_system_ini ):
    var2 = "/opt/ericsson/sck/bin/admin_final_pre_checks.bsh " +  options + " " + mws_ip + " " + path_to_orc_system_ini
    print (var2)
    run (var2)


def admin_split_cluster( options ):
    var2 = "/opt/ericsson/sck/bin/admin_split_cluster.bsh " +  options
    print (var2)
    run (var2)


def admin_upgrade_tasks( options, mws_ip, ossrc_media ):
    var2 = "/opt/ericsson/sck/bin/admin_upgrade_tasks.bsh " + options + " " + mws_ip + " " + ossrc_media
    print (var2)
    run (var2)


def admin_post_upgrade_tasks( options ):
    var2 = "/opt/ericsson/sck/bin/admin_post_upgrade_tasks.bsh " +  options
    print (var2)
    run (var2)


def admin_pre_cutover_tasks( options, mws_ip, path_to_orch_depot ):
    var2 = "/opt/ericsson/sck/bin/admin_pre_cutover_tasks.bsh " +  options + " " + mws_ip + " " + path_to_orch_depot
    print (var2)
    run (var2)


def admin_cutover_tasks( options, mws_ip, path_to_orch_depot ):
    var2 = "/opt/ericsson/sck/bin/admin_cutover_tasks.bsh " +  options + " " + mws_ip + " " + path_to_orch_depot
    print (var2)
    run (var2)


def admin_post_cutover_tasks( options ):
    var2 = "/opt/ericsson/sck/bin/admin_post_cutover_tasks.bsh " +  options
    print (var2)
    run (var2)


def admin_post_cutover_tasks_trunc( options ):
    var2 = "/opt/ericsson/sck/bin/admin_post_cutover_tasks_trunc.bsh " +  options
    print (var2)
    run (var2)


def disable_mysql():
    run('/usr/sbin/svcadm disable -s mysql')


def admin_pre_remake_cluster_tasks( options ):
    var2 = "/opt/ericsson/sck/bin/admin_pre_remake_cluster_tasks.bsh " + options
    print (var2)
    run (var2)


def admin_post_remake_cluster_tasks( options, mws_ip, path_to_orch_depot ):
    var2 = "/opt/ericsson/sck/bin/admin_post_remake_cluster_tasks.bsh " +  options + " " + mws_ip + " " + path_to_orch_depot
    print (var2)
    run (var2)


def admin_remake_cluster_tasks( options ):
    var2 = "/opt/ericsson/sck/bin/admin_remake_cluster_tasks.bsh " + options
    print (var2)
    run (var2)


def cominf_upgrade_tasks( options, server_type, admin_ip, mws_ip, cominf_media ):
    var2 = "/var/tmp/cominf/cominf_upgrade_tasks.bsh " + options + " " + server_type + " " + admin_ip + " " + mws_ip + " " + cominf_media
    print (var2)
    run(var2)


def sserv_pre_upgrade_verification(options, server_type, mws_ip, sserv_media, path_to_orch_depot, orcha_user=None):
    if orcha_user is None:
        var2 = "/var/tmp/sserv/sserv_pre_upgrade_verification.bsh " + options + " " + server_type + " " + mws_ip + " " + sserv_media + " " + path_to_orch_depot
    else:
        var2 = "su - " + orcha_user + " -c \"/usr/local/bin/sudo /var/tmp/sserv/sserv_pre_upgrade_verification.bsh " + options + " " + server_type + " " + mws_ip + " " + sserv_media + " " + path_to_orch_depot + "\""
    print (var2)
    run(var2)


def sserv_pre_upgrade_tasks(options, server_type, mws_ip, sserv_media, path_to_orch_depot, orcha_user=None):
    if orcha_user is None:
        var2 = "/var/tmp/sserv/sserv_pre_upgrade_tasks.bsh " + options + " " + server_type + " " + mws_ip + " " + sserv_media + " " + path_to_orch_depot
    else:
        var2 = "su - " + orcha_user + " -c \"/usr/local/bin/sudo /var/tmp/sserv/sserv_pre_upgrade_tasks.bsh " + options + " " + server_type + " " + mws_ip + " " + sserv_media + " " + path_to_orch_depot + "\""
    print (var2)
    run(var2)


def sserv_upgrade_tasks(options, server_type, mws_ip, sserv_media, orcha_user=None):
    if orcha_user is None:
        var2 = "/var/tmp/sserv/sserv_upgrade_tasks.bsh " + options + " " + server_type + " " + mws_ip + " " + sserv_media
    else:
        var2 = "su - " + orcha_user + " -c \"/usr/local/bin/sudo /var/tmp/sserv/sserv_upgrade_tasks.bsh " + options + " " + server_type + " " + mws_ip + " " + sserv_media + "\""
    print (var2)
    run(var2)


def sserv_post_upgrade_tasks(options, server_type, orcha_user=None):
    if orcha_user is None:
        var2 = "/var/tmp/sserv/sserv_post_upgrade_tasks.bsh " + options + " " + server_type
    else:
        var2 = "su - " + orcha_user + " -c \"/usr/local/bin/sudo /var/tmp/sserv/sserv_post_upgrade_tasks.bsh " + options + " " + server_type + "\""
    print (var2)
    run(var2)

def sserv_post_cutover_tasks(options, server_type, orcha_user=None):
    if orcha_user is None:
        var2 = "/var/tmp/sserv/sserv_post_cutover_tasks.bsh " + options + " " + server_type
    else:
        var2 = "su - " + orcha_user + " -c \"/usr/local/bin/sudo /var/tmp/sserv/sserv_post_cutover_tasks.bsh " + options + " " + server_type + "\""
    print (var2)
    run(var2)

def node_harden_upgrade_tasks(options, server_type, mws_ip, path_to_om, orcha_user=None):
    if orcha_user is None:
        var2 = "/var/tmp/Liveupgrade/node_harden_upgrade_tasks.bsh " + options + " " + server_type + " " + mws_ip + " " + path_to_om
    else:
        var2 = "su - " + orcha_user + " -c \"/usr/local/bin/sudo /var/tmp/Liveupgrade/node_harden_upgrade_tasks.bsh " + options + " " + server_type + " " + mws_ip + " " + path_to_om + "\""
    print (var2)
    run(var2)


def node_harden_reboot_tasks(options, server_type, orcha_user=None):
    if orcha_user is None:
        var2 = "/var/tmp/Liveupgrade/node_harden_reboot_tasks.bsh " + options + " " + server_type
    else:
        var2 = "su - " + orcha_user + " -c \"/usr/local/bin/sudo /var/tmp/Liveupgrade/node_harden_reboot_tasks.bsh " + options + " " + server_type + "\""
    print (var2)
    run(var2)


def node_harden_post_upgrade_tasks(options, server_type, orcha_user=None):
    if orcha_user is None:
        var2 = "/var/tmp/Liveupgrade/node_harden_post_upgrade_tasks.bsh " + options + " " + server_type
    else:
        var2 = "su - " + orcha_user + " -c \"/usr/local/bin/sudo /var/tmp/Liveupgrade/node_harden_post_upgrade_tasks.bsh " + options + " " + server_type + "\""
    print (var2)
    run(var2)


def smrs_upgrade_tasks(options, orcha_user=None):
    if orcha_user is None:
        var2 = "/opt/ericsson/nms_bismrs_mc/bin/smrs_upgrade_tasks.bsh " + options
    else:
        var2 = "su - " + orcha_user + " -c \"/usr/local/bin/sudo /opt/ericsson/nms_bismrs_mc/bin/smrs_upgrade_tasks.bsh " + options + "\""
    print (var2)
    run(var2)
