/**
 * @see https://umijs.org/zh-CN/plugins/plugin-access
 * */
export default function access(initialState: { currentUser?: API.LoginUserVO} | undefined) {
  const { currentUser } = initialState ?? {};
  // 查看当前用户
  console.log(currentUser);
  return {
    canAdmin: currentUser && currentUser.userRole === 'admin',
  };
}
