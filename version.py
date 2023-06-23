import argparse


class Project:
    def __init__(self, version, file):
        self.version = version
        self.file = file


class AndroidProject(Project):
    def __init__(self):
        super().__init__("""ext {
       appVersionName = '%s'
       appVersionCode = %d
    }
    """, 'app/version.gradle')


def tag_name(_tag):
    return _tag[_tag.rfind('/') + 1:]


def tag_code(_tag):
    _tag = tag_name(_tag)
    a = []
    for b in _tag:
        try:
            int(b)
            a.append(b)
        except BaseException:
            pass
    a = ''.join(a)
    if a:
        return int(a)
    return 0


def write_version_to_file(version_name, version_code, project: Project):
    with open(project.file, 'w', encoding='utf-8') as f:
        f.write(project.version % (version_name, version_code))


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--p', choices=['Android'])
    parser.add_argument('--tag')
    args = parser.parse_args()
    tag = args.tag
    # tag = 'github/ref/v1.7.36'
    p = ""
    if args.p == 'Android':
        p = AndroidProject()
    write_version_to_file(tag_name(tag), tag_code(tag), p)
    print('Done!')
