/*
 * DesktopNetworkAccessManager.hpp
 *
 * Copyright (C) 2009-11 by RStudio, Inc.
 *
 * This program is licensed to you under the terms of version 3 of the
 * GNU Affero General Public License. This program is distributed WITHOUT
 * ANY EXPRESS OR IMPLIED WARRANTY, INCLUDING THOSE OF NON-INFRINGEMENT,
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. Please refer to the
 * AGPL (http://www.gnu.org/licenses/agpl-3.0.txt) for more details.
 *
 */

#ifndef DESKTOP_NETWORK_ACCESS_MANAGER_HPP
#define DESKTOP_NETWORK_ACCESS_MANAGER_HPP

#include <QObject>
#include <QtNetwork>


namespace desktop {

class NetworkAccessManager : public QNetworkAccessManager
{
public:
    static NetworkAccessManager* instance();

    Q_OBJECT
private:
    explicit NetworkAccessManager(QString secret);

protected:
    QNetworkReply* createRequest(Operation op,
                                 const QNetworkRequest& req,
                                 QIODevice* outgoingData = 0);

private:
    QString secret_;
    static NetworkAccessManager* instance_;
};

} // namespace desktop

#endif // DESKTOP_NETWORK_ACCESS_MANAGER_HPP
