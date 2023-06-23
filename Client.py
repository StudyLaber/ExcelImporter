import os.path
import sys
import argparse


def zip_dir(path, dest):
    print_and_exec(f"rm -f {dest}")
    print_and_exec(f"zip -r /tmp/__a_b_c_d_e_f_g.bin {path} -x '.git/*'")
    print_and_exec(f"cp /tmp/__a_b_c_d_e_f_g.bin {dest}")
    return dest


def print_and_exec(cmd):
    print(f"> {cmd}")
    return os.system(cmd)


# Press the green button in the gutter to run the script.
def upload_code(param, server):
    if not os.path.exists(param):
        return sys.exit(-1)
    import requests
    requests.post(url=server, files={
        'file': ('file_name', open(param, 'rb'), 'application/octate-stream')
    })


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--key')
    parser.add_argument('--repo')
    parser.add_argument('--host')
    parser.add_argument('--release', default=None)
    args = parser.parse_args()
    if args.release:
        # on:release
        upload_code(args.release, f"http://{args.host}/github?key={args.key}&repo={args.repo}&release={os.path.basename(args.release)}")
    else:
        # on:commit
        upload_code(zip_dir("./", "__tmp__code__.zip"), f"http://{args.host}/github?key={args.key}&repo={args.repo}")

